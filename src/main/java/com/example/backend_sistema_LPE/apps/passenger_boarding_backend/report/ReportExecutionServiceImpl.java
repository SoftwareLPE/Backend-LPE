package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.BoardingEventApiSyncService;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.dto.BoardingEventIngestionSummary;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto.ExecuteReportRequest;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto.ExecuteReportResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.enums.ReportExecutionStatus;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon.SessionWialonService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Transactional
public class ReportExecutionServiceImpl implements ReportExecutionService {
    private static final Logger log = LoggerFactory.getLogger(ReportExecutionServiceImpl.class);
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Ojinaga");

    private final ReportExecutionRepository reportExecutionRepository;
    private final SessionWialonService sessionWialonService;
    private final WialonReportClient wialonReportClient;
    private final BoardingEventApiSyncService boardingEventApiSyncService;
    private final long reportTtlSeconds;
    private final long runningLockSeconds;
    private final ConcurrentMap<String, ReentrantLock> requestLocks = new ConcurrentHashMap<>();

    public ReportExecutionServiceImpl(
            ReportExecutionRepository reportExecutionRepository,
            SessionWialonService sessionWialonService,
            WialonReportClient wialonReportClient,
            BoardingEventApiSyncService boardingEventApiSyncService,
            @Value("${wialon.report.ttl-seconds:300}") long reportTtlSeconds,
            @Value("${wialon.report.lock-seconds:180}") long runningLockSeconds
    ) {
        this.reportExecutionRepository = reportExecutionRepository;
        this.sessionWialonService = sessionWialonService;
        this.wialonReportClient = wialonReportClient;
        this.boardingEventApiSyncService = boardingEventApiSyncService;
        this.reportTtlSeconds = reportTtlSeconds;
        this.runningLockSeconds = runningLockSeconds;
    }

    @Override
    public ExecuteReportResponse executeReport(ExecuteReportRequest request) {
        IntervalRange intervalRange = resolveIntervalRange(request.getIntervalFrom(), request.getIntervalTo());
        String requestKey = buildRequestKey(request, intervalRange.fromUnix(), intervalRange.toUnix());
        boolean forceRefresh = Boolean.TRUE.equals(request.getForceRefresh());

        if (!forceRefresh) {
            ExecuteReportResponse cachedResponse = tryBuildFreshCacheResponse(requestKey, intervalRange);
            if (cachedResponse != null) {
                return cachedResponse;
            }
        }

        ReentrantLock lock = requestLocks.computeIfAbsent(requestKey, key -> new ReentrantLock());
        lock.lock();
        try {
            if (!forceRefresh) {
                ExecuteReportResponse cachedResponse = tryBuildFreshCacheResponse(requestKey, intervalRange);
                if (cachedResponse != null) {
                    return cachedResponse;
                }
            }

            ExecuteReportResponse runningResponse = tryBuildAlreadyRunningResponse(requestKey);
            if (runningResponse != null) {
                return runningResponse;
            }

            return executeAndPersist(request, requestKey, intervalRange.fromUnix(), intervalRange.toUnix());
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                requestLocks.remove(requestKey, lock);
            }
        }
    }

    private ExecuteReportResponse executeAndPersist(
            ExecuteReportRequest request,
            String requestKey,
            long intervalFromUnix,
            long intervalToUnix
    ) {
        Instant start = Instant.now();
        String sid = sessionWialonService.getOrCreateValidSid();

        ReportExecution execution = new ReportExecution();
        execution.setReportResourceId(request.getResourceId());
        execution.setReportTemplateId(request.getTemplateId());
        execution.setReportObjectId(request.getObjectId());
        execution.setReportObjectSecId(request.getObjectSecId());
        execution.setIntervalFrom(toTimestamp(intervalFromUnix));
        execution.setIntervalTo(toTimestamp(intervalToUnix));
        execution.setExecutedAt(Timestamp.from(start));
        execution.setStatus(ReportExecutionStatus.RUNNING);
        execution.setSidUsed(sid);
        execution.setRequestKey(requestKey);
        execution = reportExecutionRepository.save(execution);

        JsonNode execResponse = null;
        JsonNode rowsResponse = null;
        BoardingEventIngestionSummary ingestionResult = new BoardingEventIngestionSummary();

        try {
            try {
                wialonReportClient.cleanupResult(sid);
            } catch (Exception ignored) {
                // Best effort cleanup before exec_report.
            }
            execResponse = execReportWithRetryOnInvalidSid(execution, sid, request, intervalFromUnix, intervalToUnix);
            sid = execution.getSidUsed();

            int tableIndex = defaultIfNull(request.getTableIndex(), 0);
            int totalRowsForSelect = extractTableTotalRows(execResponse, tableIndex);
            int indexTo = totalRowsForSelect > 0
                    ? Math.max(0, totalRowsForSelect - 1)
                    : defaultIfNull(request.getIndexTo(), 1000);
            log.info("Wialon exec_report totalRows tableIndex={} rows={}", tableIndex, totalRowsForSelect);

            rowsResponse = wialonReportClient.selectResultRows(
                    sid,
                    tableIndex,
                    defaultIfNull(request.getIndexFrom(), 0),
                    indexTo
            );
            String rowsPreview = rowsResponse == null ? "null" : rowsResponse.toString();
            log.info("Wialon select_result_rows preview={}",
                    rowsPreview.substring(0, Math.min(500, rowsPreview.length())));
            ingestionResult = boardingEventApiSyncService.ingest(
                    execution,
                    execResponse,
                    rowsResponse,
                    request.getResourceId(),
                    request.getObjectSecId(),
                    tableIndex
            );

            execution.setTotalRows(extractTotalRows(execResponse));
            execution.setRowCount(extractRowCount(rowsResponse));
            execution.setStatus(ReportExecutionStatus.COMPLETED);
            execution.setFinishedAt(Timestamp.from(Instant.now()));
            execution.setDurationMs((int) Duration.between(start, Instant.now()).toMillis());
            execution.setErrorMessage(null);
            reportExecutionRepository.save(execution);

            ExecuteReportResponse response = baseResponseFromExecution(execution);
            response.setPersistedEvents(ingestionResult.getInserted());
            response.setCandidateRows(ingestionResult.getCandidateRows());
            response.setSkippedNoCellsArray(ingestionResult.getSkippedNoCellsArray());
            response.setSkippedMissingRequiredColumns(ingestionResult.getSkippedMissingRequiredColumns());
            response.setSkippedMissingPassengerId(ingestionResult.getSkippedMissingPassengerId());
            response.setSkippedMissingUnitWialonId(ingestionResult.getSkippedMissingUnitWialonId());
            response.setSkippedDuplicateWialonRowKey(ingestionResult.getSkippedDuplicateWialonRowKey());
            response.setCacheHit(false);
            response.setAlreadyRunning(false);
            response.setInfoMessage("Report executed and ingested successfully.");
            response.setExecReportRaw(execResponse);
            response.setGetResultRowsRaw(null);
            response.setRowsRaw(rowsResponse);
            return response;
        } catch (Exception ex) {
            execution.setStatus(ReportExecutionStatus.FAILED);
            execution.setFinishedAt(Timestamp.from(Instant.now()));
            execution.setDurationMs((int) Duration.between(start, Instant.now()).toMillis());
            execution.setErrorMessage(truncate(ex.getMessage(), 255));
            reportExecutionRepository.save(execution);
            throw ex;
        } finally {
            try {
                wialonReportClient.cleanupResult(sid);
            } catch (Exception ignored) {
                // Best effort cleanup.
            }
        }
    }

    private ExecuteReportResponse tryBuildFreshCacheResponse(String requestKey, IntervalRange intervalRange) {
        return reportExecutionRepository
                .findTopByRequestKeyAndStatusOrderByExecutedAtDesc(requestKey, ReportExecutionStatus.COMPLETED)
                .filter(execution -> shouldReuseCompletedExecution(execution, intervalRange))
                .map(execution -> {
                    ExecuteReportResponse response = baseResponseFromExecution(execution);
                    response.setCacheHit(true);
                    response.setAlreadyRunning(false);
                    response.setInfoMessage(buildCacheMessage(intervalRange));
                    return response;
                })
                .orElse(null);
    }

    private ExecuteReportResponse tryBuildAlreadyRunningResponse(String requestKey) {
        Instant threshold = Instant.now().minusSeconds(runningLockSeconds);
        return reportExecutionRepository
                .findTopByRequestKeyAndStatusOrderByExecutedAtDesc(requestKey, ReportExecutionStatus.RUNNING)
                .filter(execution -> execution.getFinishedAt() == null)
                .filter(execution -> execution.getExecutedAt() != null && execution.getExecutedAt().toInstant().isAfter(threshold))
                .map(execution -> {
                    ExecuteReportResponse response = baseResponseFromExecution(execution);
                    response.setCacheHit(false);
                    response.setAlreadyRunning(true);
                    response.setInfoMessage("A sync for this same request is already running.");
                    return response;
                })
                .orElse(null);
    }

    private boolean isWithinTtl(ReportExecution execution) {
        Timestamp reference = execution.getFinishedAt() != null ? execution.getFinishedAt() : execution.getExecutedAt();
        if (reference == null) {
            return false;
        }
        Instant ttlThreshold = Instant.now().minusSeconds(reportTtlSeconds);
        return reference.toInstant().isAfter(ttlThreshold);
    }

    private boolean shouldReuseCompletedExecution(ReportExecution execution, IntervalRange intervalRange) {
        if (!includesToday(intervalRange)) {
            return true;
        }
        return isWithinTtl(execution);
    }

    private String buildCacheMessage(IntervalRange intervalRange) {
        if (includesToday(intervalRange)) {
            return "Fresh cached execution found for today. Reusing data from database.";
        }
        return "Persisted historical range found. Reusing data from database.";
    }

    private ExecuteReportResponse baseResponseFromExecution(ReportExecution execution) {
        ExecuteReportResponse response = new ExecuteReportResponse();
        response.setReportExecutionId(execution.getReportExecutionId());
        response.setStatus(execution.getStatus());
        response.setSidUsed(execution.getSidUsed());
        response.setRowCount(execution.getRowCount());
        response.setTotalRows(execution.getTotalRows());
        response.setPersistedEvents(0);
        response.setCandidateRows(0);
        response.setSkippedNoCellsArray(0);
        response.setSkippedMissingRequiredColumns(0);
        response.setSkippedMissingPassengerId(0);
        response.setSkippedMissingUnitWialonId(0);
        response.setSkippedDuplicateWialonRowKey(0);
        response.setDurationMs(execution.getDurationMs());
        response.setExecutedAt(execution.getExecutedAt());
        response.setFinishedAt(execution.getFinishedAt());
        response.setErrorMessage(execution.getErrorMessage());
        return response;
    }

    private JsonNode execReportWithRetryOnInvalidSid(
            ReportExecution execution,
            String currentSid,
            ExecuteReportRequest request,
            long intervalFromUnix,
            long intervalToUnix
    ) {
        try {
            return wialonReportClient.execReport(
                    currentSid,
                    request.getResourceId(),
                    request.getTemplateId(),
                    request.getObjectId(),
                    request.getObjectSecId(),
                    intervalFromUnix,
                    intervalToUnix
            );
        } catch (WialonApiException ex) {
            boolean isInvalidSid = "report/exec_report".equals(ex.getService()) && ex.getErrorCode() == 1;
            if (!isInvalidSid) {
                throw ex;
            }

            String refreshedSid = sessionWialonService.forceRefreshSid();
            execution.setSidUsed(refreshedSid);
            reportExecutionRepository.save(execution);

            return wialonReportClient.execReport(
                    refreshedSid,
                    request.getResourceId(),
                    request.getTemplateId(),
                    request.getObjectId(),
                    request.getObjectSecId(),
                    intervalFromUnix,
                    intervalToUnix
            );
        }
    }

    private Timestamp toTimestamp(Long unixSeconds) {
        return Timestamp.from(Instant.ofEpochSecond(unixSeconds));
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String buildRequestKey(ExecuteReportRequest request, long intervalFromUnix, long intervalToUnix) {
        return request.getResourceId() + "|" +
                request.getTemplateId() + "|" +
                request.getObjectId() + "|" +
                request.getObjectSecId() + "|" +
                intervalFromUnix + "|" +
                intervalToUnix;
    }

    private IntervalRange resolveIntervalRange(Long intervalFrom, Long intervalTo) {
        if (intervalFrom != null && intervalTo != null) {
            return new IntervalRange(intervalFrom, intervalTo);
        }

        if (intervalFrom != null) {
            LocalDate day = Instant.ofEpochSecond(intervalFrom).atZone(DEFAULT_ZONE).toLocalDate();
            long dayEnd = day.plusDays(1).atStartOfDay(DEFAULT_ZONE).toEpochSecond() - 1;
            return new IntervalRange(intervalFrom, Math.max(intervalFrom, dayEnd));
        }

        if (intervalTo != null) {
            LocalDate day = Instant.ofEpochSecond(intervalTo).atZone(DEFAULT_ZONE).toLocalDate();
            long dayStart = day.atStartOfDay(DEFAULT_ZONE).toEpochSecond();
            return new IntervalRange(Math.min(dayStart, intervalTo), intervalTo);
        }

        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        long dayStart = today.atStartOfDay(DEFAULT_ZONE).toEpochSecond();
        long dayEnd = today.plusDays(1).atStartOfDay(DEFAULT_ZONE).toEpochSecond() - 1;
        return new IntervalRange(dayStart, dayEnd);
    }

    private boolean includesToday(IntervalRange intervalRange) {
        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        long todayStart = today.atStartOfDay(DEFAULT_ZONE).toEpochSecond();
        long todayEnd = today.plusDays(1).atStartOfDay(DEFAULT_ZONE).toEpochSecond() - 1;
        return intervalRange.fromUnix() <= todayEnd && intervalRange.toUnix() >= todayStart;
    }

    private record IntervalRange(long fromUnix, long toUnix) {}

    private int extractTotalRows(JsonNode execResponse) {
        if (execResponse == null) {
            return 0;
        }
        JsonNode reportResult = execResponse.path("reportResult");
        if (!reportResult.isMissingNode()) {
            int rows = reportResult.path("rows").asInt(-1);
            if (rows >= 0) {
                return rows;
            }
        }
        return 0;
    }

    private int extractTableTotalRows(JsonNode execResponse, int tableIndex) {
        if (execResponse == null) {
            return 0;
        }
        JsonNode tables = execResponse.path("reportResult").path("tables");
        if (!tables.isArray() || tables.isEmpty()) {
            return 0;
        }
        int safeIndex = tableIndex >= 0 && tableIndex < tables.size() ? tableIndex : 0;
        JsonNode rowsNode = tables.get(safeIndex).path("rows");
        if (rowsNode.isInt() || rowsNode.isLong()) {
            return rowsNode.asInt(0);
        }
        return 0;
    }

    private int extractRowCount(JsonNode rowsResponse) {
        if (rowsResponse == null) {
            return 0;
        }
        JsonNode rows = rowsResponse.path("rows");
        if (rows.isArray()) {
            return rows.size();
        }
        if (rowsResponse.isArray()) {
            return rowsResponse.size();
        }
        return 0;
    }

    private String truncate(String value, int maxLen) {
        if (value == null || value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen);
    }
}

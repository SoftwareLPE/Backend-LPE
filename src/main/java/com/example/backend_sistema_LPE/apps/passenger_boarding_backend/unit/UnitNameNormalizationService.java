package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UnitNameNormalizationService {
    private static final Pattern ROUTE_CODE_PATTERN = Pattern.compile("R-\\d+");
    private static final Pattern INTERNAL_ID_PATTERN = Pattern.compile("ID\\s*(\\d+)$");

    public void apply(Unit unit, String unitNameRaw) {
        unit.setRouteCode(null);
        unit.setRouteName(null);
        unit.setInternalId(null);

        if (unitNameRaw == null || unitNameRaw.isBlank()) {
            return;
        }

        Matcher routeCodeMatcher = ROUTE_CODE_PATTERN.matcher(unitNameRaw);
        if (routeCodeMatcher.find()) {
            unit.setRouteCode(routeCodeMatcher.group());
        }

        Matcher internalIdMatcher = INTERNAL_ID_PATTERN.matcher(unitNameRaw);
        if (internalIdMatcher.find()) {
            unit.setInternalId(internalIdMatcher.group(1));
        }

        if (unit.getRouteCode() != null && unitNameRaw.contains(unit.getRouteCode())) {
            int from = unitNameRaw.indexOf(unit.getRouteCode()) + unit.getRouteCode().length();
            int to = unitNameRaw.indexOf("ID");
            if (to > from) {
                String routeName = unitNameRaw.substring(from, to).replace("-", " ").trim();
                if (!routeName.isBlank()) {
                    unit.setRouteName(routeName);
                }
            }
        }
    }
}

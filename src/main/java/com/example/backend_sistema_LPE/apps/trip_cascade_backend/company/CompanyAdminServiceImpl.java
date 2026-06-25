package com.example.backend_sistema_LPE.apps.trip_cascade_backend.company;

import com.example.backend_sistema_LPE.apps.shared.plant.CreateRequestPlantDTO;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantDTO;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom.FormatCatalog;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom.FormatType;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver.DriverRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver.DriverRouteRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard.CascadaStandardManualRowRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur.FlexsurManualRowRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur.FlexsurServiceDriverAssignmentRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom.FormatCatalogRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom.FormatWeekManualRowRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom.FormatTypeRepository;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal.RegalManualRowRepository;
import com.example.backend_sistema_LPE.apps.shared.role.RoleCompanyRepository;
import com.example.backend_sistema_LPE.apps.shared.role.RolePlantRepository;
import com.example.backend_sistema_LPE.apps.shared.shift.ShiftRepository;
import com.example.backend_sistema_LPE.apps.shared.user.UserCompanyRepository;
import com.example.backend_sistema_LPE.apps.shared.user.UserPlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyAdminServiceImpl implements CompanyAdminService {
    private final CompanyRepository companyRepository;
    private final PlantRepository plantRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final UserPlantRepository userPlantRepository;
    private final RoleCompanyRepository roleCompanyRepository;
    private final RolePlantRepository rolePlantRepository;
    private final DriverRepository driverRepository;
    private final DriverRouteRepository driverRouteRepository;
    private final CascadaStandardManualRowRepository cascadaStandardManualRowRepository;
    private final FlexsurManualRowRepository flexsurManualRowRepository;
    private final FlexsurServiceDriverAssignmentRepository flexsurServiceDriverAssignmentRepository;
    private final FormatCatalogRepository formatCatalogRepository;
    private final FormatWeekManualRowRepository formatWeekManualRowRepository;
    private final FormatTypeRepository formatTypeRepository;
    private final RegalManualRowRepository regalManualRowRepository;
    private final ShiftRepository shiftRepository;

    public CompanyAdminServiceImpl(
            CompanyRepository companyRepository,
            PlantRepository plantRepository,
            UserCompanyRepository userCompanyRepository,
            UserPlantRepository userPlantRepository,
            RoleCompanyRepository roleCompanyRepository,
            RolePlantRepository rolePlantRepository,
            DriverRepository driverRepository,
            DriverRouteRepository driverRouteRepository,
            CascadaStandardManualRowRepository cascadaStandardManualRowRepository,
            FlexsurManualRowRepository flexsurManualRowRepository,
            FlexsurServiceDriverAssignmentRepository flexsurServiceDriverAssignmentRepository,
            FormatCatalogRepository formatCatalogRepository,
            FormatWeekManualRowRepository formatWeekManualRowRepository,
            FormatTypeRepository formatTypeRepository,
            RegalManualRowRepository regalManualRowRepository,
            ShiftRepository shiftRepository
    ) {
        this.companyRepository = companyRepository;
        this.plantRepository = plantRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.userPlantRepository = userPlantRepository;
        this.roleCompanyRepository = roleCompanyRepository;
        this.rolePlantRepository = rolePlantRepository;
        this.driverRepository = driverRepository;
        this.driverRouteRepository = driverRouteRepository;
        this.cascadaStandardManualRowRepository = cascadaStandardManualRowRepository;
        this.flexsurManualRowRepository = flexsurManualRowRepository;
        this.flexsurServiceDriverAssignmentRepository = flexsurServiceDriverAssignmentRepository;
        this.formatCatalogRepository = formatCatalogRepository;
        this.formatWeekManualRowRepository = formatWeekManualRowRepository;
        this.formatTypeRepository = formatTypeRepository;
        this.regalManualRowRepository = regalManualRowRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public List<CompanyListDTO> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(CompanyMapper::toListDTO).toList();
    }

    @Override
    public CompanyDetailDTO getCompanyDetail(Long companyId) {
        Company company = companyRepository.findByIdWithPlants(companyId)
                .orElseThrow(()-> new RuntimeException("Company not found with id: "+companyId));
        return CompanyMapper.toDetailDTO(company);
    }

    //Endpoint que agrega plantas a Compañias existentes en caso de ser necesario
    @Override
    @Transactional
    public PlantDTO addPlantToCompany(Long companyId, CreateRequestPlantDTO dto) {
        if(dto==null || dto.getPlantName()==null|| dto.getPlantName().trim().isBlank()){
            throw new RuntimeException("Plant name is required");
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(()->new RuntimeException("Company not found " +companyId));

        Plant plant = new Plant();
        plant.setPlantName(dto.getPlantName().trim());
        plant.setFormatCatalogId(dto.getFormatCatalogId());
        plant.setFormatTypeId(resolveFormatTypeId(dto.getFormatCatalogId(), dto.getFormatTypeId()));
        plant.setCompany(company);

        Plant saved = plantRepository.save(plant);

        return new PlantDTO(
                saved.getPlantId(),
                saved.getPlantName(),
                saved.getFormatCatalogId(),
                saved.getFormatTypeId()
        );
    }


    //Endpoint para crear compañias con sus respectivas plantas
    @Override
    @Transactional
    public Company createCompany(CreateCompanyRequestDTO createCompanyRequestDTO) {
        Company company = new Company();
        company.setCompanyName(createCompanyRequestDTO.getCompanyName());

        Company savedCompany = companyRepository.save(company);

        if (createCompanyRequestDTO.getPlants()!=null){
            for (CreateRequestPlantDTO plantDTO: createCompanyRequestDTO.getPlants()){
                Plant plant = new Plant();
                plant.setPlantName(plantDTO.getPlantName());
                plant.setFormatCatalogId(plantDTO.getFormatCatalogId());
                plant.setFormatTypeId(resolveFormatTypeId(plantDTO.getFormatCatalogId(), plantDTO.getFormatTypeId()));
                plant.setCompany(savedCompany);

                plantRepository.save(plant);
            }
        }

        return savedCompany;
    }

    @Override
    @Transactional
    public UpdateCompanyNameDTO updateCompanyName(Long companyId, UpdateCompanyNameDTO updateCompanyNameDTO) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(()->new RuntimeException("Company not found"+companyId));

        company.setCompanyName(updateCompanyNameDTO.getCompanyName());

        companyRepository.save(company);

        return new UpdateCompanyNameDTO(company.getCompanyName());
    }

    @Override
    public List<CompanyTableDTO> getCompaniesForTable() {
        return companyRepository.findCompaniesForTable();
    }

    @Override
    public List<CompanyDetailDTO> getAllCompaniesWithPlants() {
        return companyRepository.findAllWithPlants().stream()
                .map(CompanyMapper::toDetailDTO)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found " + companyId));

        cascadaStandardManualRowRepository.deleteByPlantCompanyCompanyId(companyId);
        flexsurManualRowRepository.deleteByPlantCompanyCompanyId(companyId);
        flexsurServiceDriverAssignmentRepository.deleteByPlantCompanyCompanyId(companyId);
        formatWeekManualRowRepository.deleteByPlantCompanyCompanyId(companyId);
        regalManualRowRepository.deleteByPlantCompanyCompanyId(companyId);
        shiftRepository.deleteByPlantCompanyCompanyId(companyId);
        driverRouteRepository.deleteByDriverPlantCompanyCompanyId(companyId);
        driverRepository.deleteByPlantCompanyCompanyId(companyId);

        rolePlantRepository.deleteByPlantCompanyCompanyId(companyId);
        userPlantRepository.deleteByPlantCompanyCompanyId(companyId);

        plantRepository.deleteByCompanyCompanyId(companyId);

        roleCompanyRepository.deleteByCompanyCompanyId(companyId);
        userCompanyRepository.deleteByCompanyCompanyId(companyId);

        companyRepository.delete(company);
    }

    private Long resolveFormatTypeId(Long formatCatalogId, Long requestedFormatTypeId) {
        if (formatCatalogId == null) {
            throw new RuntimeException("formatCatalogId is required");
        }

        FormatCatalog formatCatalog = formatCatalogRepository.findById(formatCatalogId)
                .orElseThrow(() -> new RuntimeException("Format catalog not found " + formatCatalogId));

        if (!"CUSTOM".equalsIgnoreCase(formatCatalog.getFormatCategory())) {
            return null;
        }

        FormatType linkedFormatType = formatTypeRepository.findByFormatCatalogId(formatCatalogId)
                .orElse(null);

        if (linkedFormatType == null) {
            if (requestedFormatTypeId == null) {
                throw new RuntimeException("No formatType linked to formatCatalogId: " + formatCatalogId);
            }
            return formatTypeRepository.findById(requestedFormatTypeId)
                    .orElseThrow(() -> new RuntimeException("Format type not found " + requestedFormatTypeId))
                    .getFormatTypeId();
        }

        if (requestedFormatTypeId != null && !requestedFormatTypeId.equals(linkedFormatType.getFormatTypeId())) {
            throw new RuntimeException(
                    "formatTypeId does not match formatCatalogId. Expected "
                            + linkedFormatType.getFormatTypeId()
                            + " for formatCatalogId "
                            + formatCatalogId
            );
        }

        return linkedFormatType.getFormatTypeId();
    }

}

package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.FormatCatalogDTO;
import com.example.backend_sistema_LPE.repository.FormatCatalogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/formats")
public class FormatCatalogController {
    private final FormatCatalogRepository formatCatalogRepository;

    public FormatCatalogController(FormatCatalogRepository formatCatalogRepository) {
        this.formatCatalogRepository = formatCatalogRepository;
    }

    @GetMapping
    public ResponseEntity<List<FormatCatalogDTO>> getFormats() {
        List<FormatCatalogDTO> formats = formatCatalogRepository.findByActiveTrue().stream()
                .map(format -> new FormatCatalogDTO(
                        format.getFormatCatalogId(),
                        format.getFormatCode(),
                        format.getName(),
                        format.getFormatCategory()
                ))
                .toList();

        return ResponseEntity.ok(formats);
    }
}

package com.relatorio.transporte.controller;

import com.relatorio.transporte.entity.dto.MetricasRequest;
import com.relatorio.transporte.entity.dto.MetricasResponse;
import com.relatorio.transporte.service.MetricasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Metricas", description = "Calculo de metricas de tempo do processo logistico")
@RestController
@RequestMapping("/metricas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MetricasController {

    private final MetricasService metricasService;

    @Operation(summary = "Calcula leadTime, cycleTime, tempos de espera/pesagem/pos-carregamento e detecta gargalos")
    @SecurityRequirements
    @PostMapping
    public MetricasResponse calcular(@RequestBody MetricasRequest request) {
        return metricasService.calcular(request);
    }
}

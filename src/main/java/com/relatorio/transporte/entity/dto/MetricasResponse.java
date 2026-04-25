package com.relatorio.transporte.entity.dto;

import java.util.List;

public record MetricasResponse(
        Metricas metricas,
        List<String> inconsistencias,
        List<String> gargalos
) {
    public record Metricas(
            long leadTimeMinutos, double leadTimeHoras,
            long cycleTimeMinutos, double cycleTimeHoras,
            long tempoEsperaMinutos, double tempoEsperaHoras,
            long tempoPosCarregamentoMinutos, double tempoPosCarregamentoHoras,
            long tempoDocaMinutos, double tempoDocaHoras,
            long tempoPesoInicialMinutos, double tempoPesoInicialHoras,
            long tempoPesoFinalMinutos, double tempoPesoFinalHoras,
            long tempoTotalCDMinutos, double tempoTotalCDHoras
    ) {
    }
}

package com.relatorio.transporte.entity.dto;

public record MetricasRequest(
        String agenda_CRIACAO,
        String peso_INICIAL,
        String inicio_CARREGAMENTO,
        String final_CARREGAMENTO,
        String peso_FINAL,
        String saida,
        String chegada_CD
) {
}

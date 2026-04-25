package com.relatorio.transporte.service;

import com.relatorio.transporte.entity.dto.MetricasRequest;
import com.relatorio.transporte.entity.dto.MetricasResponse;
import com.relatorio.transporte.entity.dto.MetricasResponse.Metricas;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MetricasService {

    private static final String VAZIO = "Não houve devolução";

    private static final long LIMITE_ESPERA_HORAS = 6;
    private static final long LIMITE_CICLO_HORAS = 12;
    private static final long LIMITE_POS_CARREGAMENTO_HORAS = 6;
    private static final long LIMITE_PESAGEM_HORAS = 2;
    private static final long LIMITE_LEAD_TIME_HORAS = 24;

    public MetricasResponse calcular(MetricasRequest req) {
        List<String> inconsistencias = new ArrayList<>();
        List<String> gargalos = new ArrayList<>();

        Map<String, LocalDateTime> ts = parseTodos(req, inconsistencias);
        validarOrdem(ts, inconsistencias);

        Duration leadTime              = diferenca(ts, "agenda_CRIACAO",      "saida",               inconsistencias);
        Duration cycleTime             = diferenca(ts, "inicio_CARREGAMENTO", "final_CARREGAMENTO",  inconsistencias);
        Duration tempoEspera           = diferenca(ts, "agenda_CRIACAO",      "inicio_CARREGAMENTO", inconsistencias);
        Duration tempoPosCarregamento  = diferenca(ts, "final_CARREGAMENTO",  "saida",               inconsistencias);
        Duration tempoDoca             = diferenca(ts, "inicio_CARREGAMENTO", "final_CARREGAMENTO",  inconsistencias);
        Duration tempoPesoInicial      = diferenca(ts, "agenda_CRIACAO",      "peso_INICIAL",        inconsistencias);
        Duration tempoPesoFinal        = diferenca(ts, "final_CARREGAMENTO", "peso_FINAL",           inconsistencias);
        Duration tempoTotalCD          = diferenca(ts, "agenda_CRIACAO",      "saida",               inconsistencias);

        detectarGargalos(tempoEspera, cycleTime, tempoPosCarregamento,
                tempoPesoInicial, tempoPesoFinal, leadTime, gargalos);

        Metricas metricas = new Metricas(
                tempoEmMinutos(leadTime),             tempoEmHoras(leadTime),
                tempoEmMinutos(cycleTime),            tempoEmHoras(cycleTime),
                tempoEmMinutos(tempoEspera),          tempoEmHoras(tempoEspera),
                tempoEmMinutos(tempoPosCarregamento), tempoEmHoras(tempoPosCarregamento),
                tempoEmMinutos(tempoDoca),            tempoEmHoras(tempoDoca),
                tempoEmMinutos(tempoPesoInicial),     tempoEmHoras(tempoPesoInicial),
                tempoEmMinutos(tempoPesoFinal),       tempoEmHoras(tempoPesoFinal),
                tempoEmMinutos(tempoTotalCD),         tempoEmHoras(tempoTotalCD)
        );

        return new MetricasResponse(metricas, inconsistencias, gargalos);
    }

    private Map<String, LocalDateTime> parseTodos(MetricasRequest req, List<String> inconsistencias) {
        Map<String, String> brutos = new LinkedHashMap<>();
        brutos.put("agenda_CRIACAO",      req.agenda_CRIACAO());
        brutos.put("peso_INICIAL",        req.peso_INICIAL());
        brutos.put("inicio_CARREGAMENTO", req.inicio_CARREGAMENTO());
        brutos.put("final_CARREGAMENTO", req.final_CARREGAMENTO());
        brutos.put("peso_FINAL",         req.peso_FINAL());
        brutos.put("saida",              req.saida());
        brutos.put("chegada_CD",         req.chegada_CD());

        Map<String, LocalDateTime> ts = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : brutos.entrySet()) {
            String valor = e.getValue();
            if (valor == null || valor.isBlank() || VAZIO.equalsIgnoreCase(valor.trim())) {
                continue;
            }
            try {
                ts.put(e.getKey(), parse(valor));
            } catch (DateTimeParseException ex) {
                inconsistencias.add("Campo '" + e.getKey() + "' com formato invalido: " + valor);
            }
        }
        return ts;
    }

    private LocalDateTime parse(String valor) {
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException primeira) {
            return LocalDateTime.parse(valor, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    private void validarOrdem(Map<String, LocalDateTime> ts, List<String> inconsistencias) {
        String[][] regras = {
                {"agenda_CRIACAO", "peso_INICIAL"},
                {"agenda_CRIACAO", "inicio_CARREGAMENTO"},
                {"inicio_CARREGAMENTO", "final_CARREGAMENTO"},
                {"final_CARREGAMENTO", "peso_FINAL"},
                {"final_CARREGAMENTO", "saida"},
                {"saida", "chegada_CD"}
        };
        for (String[] r : regras) {
            LocalDateTime a = ts.get(r[0]);
            LocalDateTime b = ts.get(r[1]);
            if (a != null && b != null && b.isBefore(a)) {
                inconsistencias.add("'" + r[1] + "' anterior a '" + r[0] + "'");
            }
        }
    }

    private Duration diferenca(Map<String, LocalDateTime> ts, String inicio, String fim, List<String> inconsistencias) {
        LocalDateTime a = ts.get(inicio);
        LocalDateTime b = ts.get(fim);
        if (a == null || b == null) {
            inconsistencias.add("Nao foi possivel calcular intervalo entre '" + inicio + "' e '" + fim + "' (campo ausente)");
            return Duration.ZERO;
        }
        if (b.isBefore(a)) {
            return Duration.ZERO;
        }
        return Duration.between(a, b);
    }

    private void detectarGargalos(Duration espera, Duration ciclo, Duration pos,
                                  Duration pesoIni, Duration pesoFim, Duration lead,
                                  List<String> gargalos) {
        if (espera.toHours() > LIMITE_ESPERA_HORAS) {
            gargalos.add("Tempo de espera elevado: " + tempoEmHoras(espera) + "h (limite " + LIMITE_ESPERA_HORAS + "h)");
        }
        if (ciclo.toHours() > LIMITE_CICLO_HORAS) {
            gargalos.add("Cycle time elevado: " + tempoEmHoras(ciclo) + "h (limite " + LIMITE_CICLO_HORAS + "h)");
        }
        if (pos.toHours() > LIMITE_POS_CARREGAMENTO_HORAS) {
            gargalos.add("Pos-carregamento elevado: " + tempoEmHoras(pos) + "h (limite " + LIMITE_POS_CARREGAMENTO_HORAS + "h)");
        }
        if (pesoIni.toHours() > LIMITE_PESAGEM_HORAS) {
            gargalos.add("Pesagem inicial demorada: " + tempoEmHoras(pesoIni) + "h (limite " + LIMITE_PESAGEM_HORAS + "h)");
        }
        if (pesoFim.toHours() > LIMITE_PESAGEM_HORAS) {
            gargalos.add("Pesagem final demorada: " + tempoEmHoras(pesoFim) + "h (limite " + LIMITE_PESAGEM_HORAS + "h)");
        }
        if (lead.toHours() > LIMITE_LEAD_TIME_HORAS) {
            gargalos.add("Lead time elevado: " + tempoEmHoras(lead) + "h (limite " + LIMITE_LEAD_TIME_HORAS + "h)");
        }
    }

    private long tempoEmMinutos(Duration d) {
        return d == null ? 0 : d.toMinutes();
    }

    private double tempoEmHoras(Duration d) {
        if (d == null) return 0.0;
        return Math.round(d.toMinutes() / 60.0 * 100.0) / 100.0;
    }
}

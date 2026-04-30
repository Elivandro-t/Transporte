package com.relatorio.transporte;

import com.relatorio.transporte.entity.sqlserver.ConhecimentoProjection;
import com.relatorio.transporte.repository.sqlserver.ConhecimentoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ServiceRelatorios {
    @Autowired
    private ConhecimentoRepository repository;
    private static final List<String> TODAS_FILIAIS = List.of("116","87","331","338","336","342","345","84","344");

    @Value("${app.sla.threshold-hours:72}")
    private long slaThresholdHours;

    @Value("${app.sla.dias-busca:60}")
    private int slaDiasBusca;

    public Page<ConhecimentoProjection> buscarPaginado(int pagina) {
        Pageable pageable = PageRequest.of(pagina, 100, Sort.by("conhecimento").descending());
        return repository.buscarConhecimentos(TODAS_FILIAIS, 60, List.of(22, 5), 7,null, pageable);
    }
    @Cacheable(
            value = "conhecimentos",
            key = "(#filial ?: 'todas') + '_' + #page + '_' + #size + '_' + (#placa ?: 'todas')"
    )    public ResultadoBanco buscarNoBanco(String filial, int page, Integer size,String placa) {
        long inicio = System.currentTimeMillis();

        List<String> filiais = (filial == null || filial.isBlank()) ? TODAS_FILIAIS : List.of(filial);

        Sort sort = Sort.by(Sort.Direction.DESC, "CP04_ID");
        Pageable pageable = (size == null)
                ? Pageable.unpaged(sort)
                : PageRequest.of(page, size, sort);

        log.info("Iniciando consulta SQL Server: filiais={} dias={} pageable={}", filiais, 60, pageable);
        String placaFiltro = (placa == null || placa.isBlank()) ? null : placa;
        System.out.println("Placa do veiculo "+placaFiltro);

        Page<ConhecimentoProjection> resultado = repository.buscarConhecimentos(filiais, 60, List.of(22, 5), 7,placaFiltro, pageable);

        long tempoBanco = System.currentTimeMillis() - inicio;
        log.info("Consulta concluida em {}ms. Linhas retornadas={}, totalElements={}",
                tempoBanco, resultado.getNumberOfElements(), resultado.getTotalElements());

        return new ResultadoBanco(tempoBanco, resultado);
    }

    @Cacheable(
            value = "rankingPlacas",
            key = "(#filtro.placa() ?: '_') + ':' + (#filtro.conhecimento() ?: '_') + ':' + (#filtro.dataDe() ?: '_') + ':' + (#filtro.dataAte() ?: '_')"
    )
    public List<PlacaRanking> top20PioresPlacas(FiltroRanking filtro) {
        Pageable pageable = Pageable.unpaged(Sort.by(Sort.Direction.DESC, "CP04_ID"));
        Page<ConhecimentoProjection> page = repository.buscarConhecimentos(
                TODAS_FILIAIS, slaDiasBusca, List.of(22, 5), 7,null, pageable);

        long thresholdSec = Duration.ofHours(slaThresholdHours).getSeconds();

        Stream<ConhecimentoProjection> stream = page.getContent().stream()
                .filter(p -> matchPlaca(p, filtro.placa()))
                .filter(p -> matchConhecimento(p, filtro.conhecimento()))
                .filter(p -> matchData(p, filtro.dataDe(), filtro.dataAte()));

        Map<String, List<Duration>> tempoPorPlaca = stream
                .map(p -> Map.entry(placaOuVazia(p.getCP04_PLACA_VEICULO()), cicloCompleto(p)))
                .filter(e -> !e.getValue().isZero() && e.getValue().getSeconds() > thresholdSec)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        return tempoPorPlaca.entrySet().stream()
                .map(e -> new PlacaRanking(
                        e.getKey(),
                        e.getValue().size(),
                        formatDuration(media(e.getValue())),
                        formatDuration(maximo(e.getValue()))))
                .sorted(Comparator
                        .comparingLong(PlacaRanking::ocorrencias).reversed()
                        .thenComparing(PlacaRanking::placa))
                .limit(20)
                .toList();
    }

    private boolean matchPlaca(ConhecimentoProjection p, String placa) {
        if (placa == null || placa.isBlank()) return true;
        String real = p.getCP04_PLACA_VEICULO();
        return real != null && real.toUpperCase().contains(placa.toUpperCase());
    }

    private boolean matchConhecimento(ConhecimentoProjection p, Integer conhecimento) {
        return conhecimento == null || conhecimento.equals(p.getCONHECIMENTO());
    }

    private boolean matchData(ConhecimentoProjection p, LocalDate de, LocalDate ate) {
        if (de == null && ate == null) return true;
        LocalDateTime criacao = p.getCRIACAO_CONHECIMENTO();
        if (criacao == null) return false;
        LocalDate d = criacao.toLocalDate();
        if (de != null && d.isBefore(de)) return false;
        if (ate != null && d.isAfter(ate)) return false;
        return true;
    }

    private Duration cicloCompleto(ConhecimentoProjection p) {
        LocalDateTime inicio = p.getCRIACAO_CONHECIMENTO();
        LocalDateTime fim = primeiroNaoNulo(
                p.getFINAL_ACERTO(),
                p.getINICIO_ACERTO(),
                p.getCHEGADA_CD(),
                p.getSAIDA());
        if (inicio == null || fim == null || fim.isBefore(inicio)) return Duration.ZERO;
        return Duration.between(inicio, fim);
    }

    private LocalDateTime primeiroNaoNulo(LocalDateTime... ts) {
        for (LocalDateTime t : ts) if (t != null) return t;
        return null;
    }

    private String placaOuVazia(String placa) {
        return (placa == null || placa.isBlank()) ? "SEM_PLACA" : placa.trim().toUpperCase();
    }

    private Duration media(List<Duration> list) {
        long total = list.stream().mapToLong(Duration::getSeconds).sum();
        return Duration.ofSeconds(total / list.size());
    }

    private Duration maximo(List<Duration> list) {
        return list.stream().max(Comparator.comparingLong(Duration::getSeconds)).orElse(Duration.ZERO);
    }

    private String formatDuration(Duration d) {
        long s = d.getSeconds();
        long days = s / 86400;
        long hours = (s % 86400) / 3600;
        long minutes = (s % 3600) / 60;
        return String.format("%dd %02dh %02dm", days, hours, minutes);
    }

    @Cacheable(
            value = "buscaConhecimento",
            key = "(#conhecimento ?: '_') + ':' + (#motorista ?: '_') + ':' + (#placa ?: '_')"
    )
    public List<ConhecimentoProjection> buscarConhecimento(Integer conhecimento, String motorista, String placa) {
        boolean semFiltro = conhecimento == null
                && (motorista == null || motorista.isBlank())
                && (placa == null || placa.isBlank());
        if (semFiltro) {
            throw new IllegalArgumentException("Informe ao menos um filtro: conhecimento, motorista ou placa");
        }

        Pageable pageable = Pageable.unpaged(Sort.by(Sort.Direction.DESC, "CP04_ID"));
        Page<ConhecimentoProjection> page = repository.buscarConhecimentos(
                TODAS_FILIAIS, slaDiasBusca, List.of(22, 5), 7,null, pageable);

        return page.getContent().stream()
                .filter(p -> matchConhecimento(p, conhecimento))
                .filter(p -> matchMotorista(p, motorista))
                .filter(p -> matchPlaca(p, placa))
                .toList();
    }

    private boolean matchMotorista(ConhecimentoProjection p, String motorista) {
        if (motorista == null || motorista.isBlank()) return true;
        String real = p.getMOTORISTA();
        return real != null && real.toUpperCase().contains(motorista.toUpperCase());
    }

    public record FiltroRanking(String placa, Integer conhecimento, LocalDate dataDe, LocalDate dataAte) {}

    public record PlacaRanking(String placa, long ocorrencias, String tempoMedio, String tempoMaximo) {}

    public record ResultadoBanco(long tempoBancoMs, Page<ConhecimentoProjection> page) {}
}

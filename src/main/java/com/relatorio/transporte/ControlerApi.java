package com.relatorio.transporte;

import com.relatorio.transporte.entity.sqlserver.ConhecimentoProjection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Relatorios", description = "Consultas de conhecimentos no SQL Server e ranking de SLA")
@RestController
@RequestMapping("/relatorios")
@CrossOrigin(origins = "*")
public class ControlerApi {
    @Autowired
    private ServiceRelatorios serviceRelatorios;

    @Operation(summary = "Lista conhecimentos paginado (100 por pagina, fixo)")
    @GetMapping("/conhecimentos")
    public Page<ConhecimentoProjection> listarPaginado(
            @Parameter(description = "Numero da pagina (0-based)") @RequestParam(defaultValue = "0") int page) {
        return serviceRelatorios.buscarPaginado(page);
    }

    @Operation(summary = "Lista conhecimentos com filtro por filial e tamanho ajustavel",
            description = "Sem 'size' aplica teto automatico para evitar resposta gigante. Cap maximo: 5000 por pagina.")
    @GetMapping("/conhecimentos/todos")
    public ServiceRelatorios.ResultadoBanco listarTodos(
            @Parameter(description = "Codigo da filial (ex: 87). Vazio = todas as filiais") @RequestParam(value = "filial", required = false) String filial,
            @Parameter(description = "Numero da pagina (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina. Sem valor traz tudo (com teto)") @RequestParam(value = "size", required = false) Integer size) {
        return serviceRelatorios.buscarNoBanco(filial, page, size);
    }

    @Operation(summary = "Top 20 piores placas com SLA vencido",
            description = "Calcula ciclo (criacao -> final acerto) e ordena placas por numero de ocorrencias acima do threshold.")
    @GetMapping("/ranking/placas")
    public List<ServiceRelatorios.PlacaRanking> top20PioresPlacas(
            @Parameter(description = "Filtro parcial pela placa") @RequestParam(value = "placa", required = false) String placa,
            @Parameter(description = "Filtro exato pelo numero do conhecimento") @RequestParam(value = "conhecimento", required = false) Integer conhecimento,
            @Parameter(description = "Data inicial de criacao do conhecimento (YYYY-MM-DD)") @RequestParam(value = "dataDe", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDe,
            @Parameter(description = "Data final de criacao do conhecimento (YYYY-MM-DD)") @RequestParam(value = "dataAte", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAte) {
        return serviceRelatorios.top20PioresPlacas(
                new ServiceRelatorios.FiltroRanking(placa, conhecimento, dataDe, dataAte));
    }

    @Operation(summary = "Busca conhecimentos por conhecimento, motorista (contains) ou placa (contains)",
            description = "Pelo menos um filtro deve ser informado. Filtros combinam com AND.")
    @GetMapping("/conhecimento/buscar")
    public List<ConhecimentoProjection> buscarConhecimento(
            @Parameter(description = "Numero exato do conhecimento (CP04_ID)") @RequestParam(value = "conhecimento", required = false) Integer conhecimento,
            @Parameter(description = "Texto contido no nome/codigo do motorista") @RequestParam(value = "motorista", required = false) String motorista,
            @Parameter(description = "Texto contido na placa do veiculo") @RequestParam(value = "placa", required = false) String placa) {
        return serviceRelatorios.buscarConhecimento(conhecimento, motorista, placa);
    }
}

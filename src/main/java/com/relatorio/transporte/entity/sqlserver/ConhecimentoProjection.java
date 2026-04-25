package com.relatorio.transporte.entity.sqlserver;

import java.time.LocalDateTime;

public interface ConhecimentoProjection {
    Integer getCONHECIMENTO();
    String getSTATUS_CONHECIMENTO();
    String getFILIAL();
    String getTIPO_CONHECIMENTO();
    String getCLASSIFICACAO_MOTORISTA();
    String getMOTORISTA();
    String getCP04_PLACA_VEICULO();
    Integer getCP04_ID_TRANSPORTADOR();
    LocalDateTime getAGENDA_CRIACAO();
    LocalDateTime getCRIACAO_CONHECIMENTO();
    LocalDateTime getPESO_INICIAL();
    LocalDateTime getINICIO_CARREGAMENTO();
    LocalDateTime getFINAL_CARREGAMENTO();
    LocalDateTime getPESO_FINAL();
    LocalDateTime getSAIDA();
    LocalDateTime getCHEGADA_CD();
    String getINICIO_DEVOLUCAO();
    String getFINAL_DEVOLUCAO();
    LocalDateTime getINICIO_ACERTO();
    LocalDateTime getFINAL_ACERTO();
}

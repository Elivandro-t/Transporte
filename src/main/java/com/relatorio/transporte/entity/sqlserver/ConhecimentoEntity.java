package com.relatorio.transporte.entity.sqlserver;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ConhecimentoEntity {
    @Id
    private Integer id;

    public ConhecimentoEntity() {}
}

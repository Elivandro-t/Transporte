package com.relatorio.transporte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // <- ativa o cache
public class TransporteApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransporteApplication.class, args);
	}

}

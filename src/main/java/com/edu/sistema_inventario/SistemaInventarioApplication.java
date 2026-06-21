package com.edu.sistema_inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class SistemaInventarioApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaInventarioApplication.class, args);
	}

}

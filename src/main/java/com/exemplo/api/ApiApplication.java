package com.exemplo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal de inicialização da API REST Spring Boot.
 * Padrão Arquitetural: MVC (Model-View-Controller)
 * Desafio Bootcamp Arquiteto(a) de Software
 */
@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
        System.out.println("=================================================");
        System.out.println(">>> API REST Clientes inicializada com sucesso!");
        System.out.println(">>> Endpoints base: http://localhost:8080/clientes");
        System.out.println(">>> Persistência:   PostgreSQL + Flyway Migrations");
        System.out.println("=================================================");
    }
}

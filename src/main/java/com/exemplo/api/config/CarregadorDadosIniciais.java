package com.exemplo.api.config;

import com.exemplo.api.model.Cliente;
import com.exemplo.api.repository.ClienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Carga de dados inicial para testes imediatos no banco de dados H2.
 */
@Configuration
public class CarregadorDadosIniciais {

    @Bean
    public CommandLineRunner carregarDados(ClienteRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                Cliente c1 = new Cliente("TechCorp Brasil", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");
                Cliente c2 = new Cliente("Logística Express", "operacoes@logexpress.com", "(21) 97654-3210", "ATIVO");
                Cliente c3 = new Cliente("Alpha Varejo Online", "comercial@alphavarejo.com", "(31) 96543-2109", "ATIVO");
                Cliente c4 = new Cliente("Global Soluções Digitais", "suporte@globalsolucoes.com", "(41) 95432-1098", "INATIVO");

                repository.saveAll(Arrays.asList(c1, c2, c3, c4));
                System.out.println(">>> [H2 Database] 4 clientes de teste inseridos com sucesso na base!");
            }
        };
    }
}

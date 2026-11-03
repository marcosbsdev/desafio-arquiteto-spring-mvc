package com.exemplo.api.repository;

import com.exemplo.api.model.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Persistência & Migrations (JPA + Flyway): ClienteRepositoryTest")
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    @DisplayName("Deve validar que as migrations V1 e V2 do Flyway popularam os 4 clientes iniciais")
    void deveCarregarDadosIniciaisViaFlywayMigration() {
        long count = clienteRepository.count();
        assertThat(count).isEqualTo(4L);
    }

    @Test
    @DisplayName("Deve buscar clientes por nome parcial ignorando maiúsculas/minúsculas")
    void deveBuscarPorNomeContendoIgnoreCase() {
        List<Cliente> encontrados = clienteRepository.findByNomeContainingIgnoreCase("techcorp");
        assertThat(encontrados)
                .hasSize(1)
                .first()
                .satisfies(c -> {
                    assertThat(c.getNome()).isEqualTo("TechCorp Brasil");
                    assertThat(c.getEmail()).isEqualTo("contato@techcorp.com.br");
                });
    }

    @Test
    @DisplayName("Deve verificar existência e buscar cliente por e-mail único")
    void deveVerificarExistenciaEBuscarPorEmail() {
        boolean existe = clienteRepository.existsByEmail("operacoes@logexpress.com");
        Optional<Cliente> cliente = clienteRepository.findByEmail("operacoes@logexpress.com");

        assertThat(existe).isTrue();
        assertThat(cliente).isPresent();
        assertThat(cliente.get().getNome()).isEqualTo("Logística Express");
    }
}

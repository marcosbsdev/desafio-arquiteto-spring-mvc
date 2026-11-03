package com.exemplo.api.domain;

import com.exemplo.api.dto.ClienteDTO;
import com.exemplo.api.model.Cliente;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Domínio: Invariantes da Entidade Cliente e ClienteDTO (DDD)")
class ClienteDomainTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Invariantes de Validação da Entidade Cliente")
    class InvariantesEntidadeCliente {

        @Test
        @DisplayName("Dado um cliente com dados válidos, quando validado, então não deve possuir violações")
        void deveAceitarClienteValido() {
            // Given (Dado)
            Cliente cliente = new Cliente(1L, "TechCorp Brasil", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");

            // When (Quando)
            Set<ConstraintViolation<Cliente>> violacoes = validator.validate(cliente);

            // Then (Então)
            assertThat(violacoes).isEmpty();
            assertThat(cliente.getId()).isEqualTo(1L);
            assertThat(cliente.getNome()).isEqualTo("TechCorp Brasil");
            assertThat(cliente.getEmail()).isEqualTo("contato@techcorp.com.br");
            assertThat(cliente.getTelefone()).isEqualTo("(11) 98765-4321");
            assertThat(cliente.getStatus()).isEqualTo("ATIVO");
            assertThat(cliente.toString()).contains("TechCorp Brasil", "contato@techcorp.com.br");
        }

        @Test
        @DisplayName("Dado um cliente com nome em branco, quando validado, então deve rejeitar por violação de obrigatoriedade")
        void deveRejeitarNomeEmBranco() {
            // Given
            Cliente cliente = new Cliente("", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");

            // When
            Set<ConstraintViolation<Cliente>> violacoes = validator.validate(cliente);

            // Then
            assertThat(violacoes).isNotEmpty();
            assertThat(violacoes)
                    .extracting(ConstraintViolation::getPropertyPath)
                    .extracting(Object::toString)
                    .contains("nome");
        }

        @Test
        @DisplayName("Dado um cliente com nome menor que 2 caracteres, quando validado, então deve rejeitar por tamanho inválido")
        void deveRejeitarNomeMuitoCurto() {
            // Given
            Cliente cliente = new Cliente("A", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");

            // When
            Set<ConstraintViolation<Cliente>> violacoes = validator.validate(cliente);

            // Then
            assertThat(violacoes)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("O nome deve ter entre 2 e 100 caracteres");
        }

        @Test
        @DisplayName("Dado um cliente com formato de e-mail inválido, quando validado, então deve rejeitar")
        void deveRejeitarEmailComFormatoInvalido() {
            // Given
            Cliente cliente = new Cliente("TechCorp Brasil", "email-invalido-sem-arroba", "(11) 98765-4321", "ATIVO");

            // When
            Set<ConstraintViolation<Cliente>> violacoes = validator.validate(cliente);

            // Then
            assertThat(violacoes)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("O e-mail deve ser válido");
        }
    }

    @Nested
    @DisplayName("Mapeamento e Contrato de ClienteDTO")
    class MapeamentoClienteDTO {

        @Test
        @DisplayName("Dado uma entidade Cliente, quando convertida para ClienteDTO e de volta para Entidade, então deve preservar o estado")
        void deveConverterEntreEntidadeEDtoPreservandoEstado() {
            // Given
            Cliente entidadeOriginal = new Cliente(10L, "Alpha Varejo", "comercial@alpha.com", "(31) 96543-2109", "ATIVO");

            // When
            ClienteDTO dto = new ClienteDTO(entidadeOriginal);
            Cliente entidadeReconstruida = dto.toEntity();

            // Then
            assertThat(dto.getId()).isEqualTo(10L);
            assertThat(dto.getNome()).isEqualTo("Alpha Varejo");
            assertThat(dto.getEmail()).isEqualTo("comercial@alpha.com");
            assertThat(entidadeReconstruida.getId()).isEqualTo(entidadeOriginal.getId());
            assertThat(entidadeReconstruida.getNome()).isEqualTo(entidadeOriginal.getNome());
            assertThat(entidadeReconstruida.getEmail()).isEqualTo(entidadeOriginal.getEmail());
            assertThat(entidadeReconstruida.getTelefone()).isEqualTo(entidadeOriginal.getTelefone());
            assertThat(entidadeReconstruida.getStatus()).isEqualTo(entidadeOriginal.getStatus());
        }
    }
}

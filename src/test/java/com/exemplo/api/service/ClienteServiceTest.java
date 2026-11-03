package com.exemplo.api.service;

import com.exemplo.api.model.Cliente;
import com.exemplo.api.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unidade & Comportamento (BDD/DDD): ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Nested
    @DisplayName("Consultas de Clientes (Query Operations)")
    class ConsultasDeClientes {

        @Test
        @DisplayName("Dado clientes cadastrados, quando listarTodos for chamado, então deve retornar a lista completa")
        void deveListarTodosOsClientes() {
            // Given (Dado)
            Cliente c1 = new Cliente(1L, "TechCorp Brasil", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");
            Cliente c2 = new Cliente(2L, "Logística Express", "operacoes@logexpress.com", "(21) 97654-3210", "ATIVO");
            given(clienteRepository.findAll()).willReturn(List.of(c1, c2));

            // When (Quando)
            List<Cliente> resultado = clienteService.listarTodos();

            // Then (Então)
            assertThat(resultado).hasSize(2).containsExactly(c1, c2);
            then(clienteRepository).should().findAll();
        }

        @Test
        @DisplayName("Dado um ID existente, quando buscarPorId for chamado, então deve retornar o cliente correspondente")
        void deveBuscarClientePorIdExistente() {
            // Given
            Cliente cliente = new Cliente(1L, "TechCorp Brasil", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");
            given(clienteRepository.findById(1L)).willReturn(Optional.of(cliente));

            // When
            Optional<Cliente> resultado = clienteService.buscarPorId(1L);

            // Then
            assertThat(resultado).isPresent().contains(cliente);
            then(clienteRepository).should().findById(1L);
        }

        @Test
        @DisplayName("Dado um termo de nome parcial com correspondência, quando buscarPorNome for chamado, então deve retornar sem acionar fallback")
        void deveBuscarPorNomeParcialIgnorandoCase() {
            // Given
            Cliente cliente = new Cliente(1L, "TechCorp Brasil", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");
            given(clienteRepository.findByNomeContainingIgnoreCase("tech")).willReturn(List.of(cliente));

            // When
            List<Cliente> resultado = clienteService.buscarPorNome("tech");

            // Then
            assertThat(resultado).hasSize(1).containsExactly(cliente);
            then(clienteRepository).should().findByNomeContainingIgnoreCase("tech");
            then(clienteRepository).should(never()).findByNome(any());
        }

        @Test
        @DisplayName("Dado que a busca parcial não encontrou registros, quando buscarPorNome for chamado, então deve acionar fallback de busca exata")
        void deveAcionarFallbackBuscaExataQuandoParcialVazia() {
            // Given
            given(clienteRepository.findByNomeContainingIgnoreCase("Inexistente")).willReturn(Collections.emptyList());
            given(clienteRepository.findByNome("Inexistente")).willReturn(Collections.emptyList());

            // When
            List<Cliente> resultado = clienteService.buscarPorNome("Inexistente");

            // Then
            assertThat(resultado).isEmpty();
            then(clienteRepository).should().findByNomeContainingIgnoreCase("Inexistente");
            then(clienteRepository).should().findByNome("Inexistente");
        }

        @Test
        @DisplayName("Dado registros na base, quando contarClientes for chamado, então deve retornar o total exato")
        void deveContarTotalDeClientes() {
            // Given
            given(clienteRepository.count()).willReturn(4L);

            // When
            long total = clienteService.contarClientes();

            // Then
            assertThat(total).isEqualTo(4L);
            then(clienteRepository).should().count();
        }
    }

    @Nested
    @DisplayName("Regras de Negócio de Cadastro (Salvar)")
    class CadastroDeCliente {

        @Test
        @DisplayName("Dado um novo cliente sem status informado e com e-mail único, quando salvar, então deve atribuir status ATIVO e persistir")
        void deveSalvarClienteDefinindoStatusAtivoPorPadrao() {
            // Given
            Cliente novoCliente = new Cliente("Parceiro Novo", "novo@parceiro.com", "(11) 91234-5678", null);
            given(clienteRepository.existsByEmail("novo@parceiro.com")).willReturn(false);
            given(clienteRepository.save(any(Cliente.class))).willAnswer(invocation -> {
                Cliente salvo = invocation.getArgument(0);
                salvo.setId(5L);
                return salvo;
            });

            // When
            Cliente resultado = clienteService.salvar(novoCliente);

            // Then
            assertThat(resultado.getId()).isEqualTo(5L);
            assertThat(resultado.getStatus()).isEqualTo("ATIVO");
            then(clienteRepository).should().existsByEmail("novo@parceiro.com");
            then(clienteRepository).should().save(novoCliente);
        }

        @Test
        @DisplayName("Dado um novo cliente com e-mail já existente, quando salvar, então deve lançar IllegalArgumentException e impedir persistência")
        void deveImpedirCadastroComEmailDuplicado() {
            // Given
            Cliente clienteDuplicado = new Cliente("Clone TechCorp", "contato@techcorp.com.br", "(11) 90000-0000", "ATIVO");
            given(clienteRepository.existsByEmail("contato@techcorp.com.br")).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> clienteService.salvar(clienteDuplicado))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Já existe um cliente cadastrado com o e-mail: contato@techcorp.com.br");

            then(clienteRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("Regras de Negócio de Atualização e Exclusão")
    class AtualizacaoEExclusao {

        @Test
        @DisplayName("Dado um cliente existente, quando atualizar seus dados, então deve persistir as alterações")
        void deveAtualizarClienteExistente() {
            // Given
            Cliente existente = new Cliente(1L, "TechCorp Brasil", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");
            Cliente dadosNovos = new Cliente("TechCorp Global", "global@techcorp.com.br", "(11) 99999-0000", "SUSPENSO");
            given(clienteRepository.findById(1L)).willReturn(Optional.of(existente));
            given(clienteRepository.save(any(Cliente.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            Optional<Cliente> atualizado = clienteService.atualizar(1L, dadosNovos);

            // Then
            assertThat(atualizado).isPresent();
            assertThat(atualizado.get().getNome()).isEqualTo("TechCorp Global");
            assertThat(atualizado.get().getEmail()).isEqualTo("global@techcorp.com.br");
            assertThat(atualizado.get().getTelefone()).isEqualTo("(11) 99999-0000");
            assertThat(atualizado.get().getStatus()).isEqualTo("SUSPENSO");
        }

        @Test
        @DisplayName("Dado um ID existente, quando deletar for chamado, então deve remover o registro e retornar true")
        void deveDeletarClienteExistente() {
            // Given
            given(clienteRepository.existsById(1L)).willReturn(true);

            // When
            boolean deletado = clienteService.deletar(1L);

            // Then
            assertThat(deletado).isTrue();
            then(clienteRepository).should().deleteById(1L);
        }

        @Test
        @DisplayName("Dado um ID inexistente, quando deletar for chamado, então não deve chamar deleteById e deve retornar false")
        void naoDeveDeletarQuandoIdInexistente() {
            // Given
            given(clienteRepository.existsById(999L)).willReturn(false);

            // When
            boolean deletado = clienteService.deletar(999L);

            // Then
            assertThat(deletado).isFalse();
            then(clienteRepository).should(never()).deleteById(any());
        }
    }
}

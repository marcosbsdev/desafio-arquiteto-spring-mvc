package com.exemplo.api.controller;

import com.exemplo.api.config.GlobalExceptionHandler;
import com.exemplo.api.model.Cliente;
import com.exemplo.api.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Camada MVC (Controller & ExceptionHandler): ClienteControllerTest")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @Test
    @DisplayName("GET /clientes - Deve retornar 200 OK e lista de clientes em JSON")
    void deveListarTodosOsClientes() throws Exception {
        Cliente c1 = new Cliente(1L, "TechCorp Brasil", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");
        given(clienteService.listarTodos()).willReturn(List.of(c1));

        mockMvc.perform(get("/clientes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nome", is("TechCorp Brasil")));
    }

    @Test
    @DisplayName("GET /clientes/{id} - Deve retornar 200 OK quando cliente existir")
    void deveRetornarClientePorIdQuandoExistir() throws Exception {
        Cliente c1 = new Cliente(1L, "TechCorp Brasil", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");
        given(clienteService.buscarPorId(1L)).willReturn(Optional.of(c1));

        mockMvc.perform(get("/clientes/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("contato@techcorp.com.br")));
    }

    @Test
    @DisplayName("GET /clientes/{id} - Deve retornar 404 Not Found quando cliente não existir")
    void deveRetornar404QuandoClienteNaoExistir() throws Exception {
        given(clienteService.buscarPorId(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/clientes/999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /clientes/nome/{nome} - Deve retornar 200 OK com clientes filtrados por nome")
    void deveBuscarClientesPorNome() throws Exception {
        Cliente c1 = new Cliente(1L, "TechCorp Brasil", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");
        given(clienteService.buscarPorNome("TechCorp")).willReturn(List.of(c1));

        mockMvc.perform(get("/clientes/nome/TechCorp").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("TechCorp Brasil")));
    }

    @Test
    @DisplayName("GET /clientes/contar - Deve retornar 200 OK com a contagem total de clientes")
    void deveRetornarContagemDeClientes() throws Exception {
        given(clienteService.contarClientes()).willReturn(4L);

        mockMvc.perform(get("/clientes/contar").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("4"));
    }

    @Test
    @DisplayName("POST /clientes - Deve retornar 201 Created ao cadastrar cliente válido")
    void deveCriarNovoClienteValido() throws Exception {
        Cliente entrada = new Cliente("Novo Parceiro", "novo@parceiro.com", "(11) 91111-2222", "ATIVO");
        Cliente salvo = new Cliente(5L, "Novo Parceiro", "novo@parceiro.com", "(11) 91111-2222", "ATIVO");
        given(clienteService.salvar(any(Cliente.class))).willReturn(salvo);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.nome", is("Novo Parceiro")));
    }

    @Test
    @DisplayName("POST /clientes - Deve retornar 400 Bad Request quando payload violar Bean Validation")
    void deveRetornar400QuandoPayloadInvalido() throws Exception {
        Cliente invalido = new Cliente("", "email-invalido", "123", "ATIVO");

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Erro de Validação")))
                .andExpect(jsonPath("$.campos.email", is("O e-mail deve ser válido")));
    }

    @Test
    @DisplayName("POST /clientes - Deve retornar 409 Conflict quando e-mail já estiver cadastrado")
    void deveRetornar409QuandoEmailDuplicado() throws Exception {
        Cliente duplicado = new Cliente("TechCorp Clone", "contato@techcorp.com.br", "(11) 98765-4321", "ATIVO");
        given(clienteService.salvar(any(Cliente.class)))
                .willThrow(new IllegalArgumentException("Já existe um cliente cadastrado com o e-mail: contato@techcorp.com.br"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflito de Regra de Negócio")));
    }

    @Test
    @DisplayName("PUT /clientes/{id} - Deve retornar 200 OK ao atualizar cliente existente")
    void deveAtualizarClienteExistente() throws Exception {
        Cliente atualizado = new Cliente(1L, "TechCorp Atualizada", "contato@techcorp.com.br", "(11) 90000-0000", "ATIVO");
        given(clienteService.atualizar(eq(1L), any(Cliente.class))).willReturn(Optional.of(atualizado));

        mockMvc.perform(put("/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("TechCorp Atualizada")));
    }

    @Test
    @DisplayName("DELETE /clientes/{id} - Deve retornar 204 No Content ao excluir cliente existente")
    void deveDeletarClienteExistente() throws Exception {
        given(clienteService.deletar(1L)).willReturn(true);

        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isNoContent());
    }
}

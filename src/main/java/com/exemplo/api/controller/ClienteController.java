package com.exemplo.api.controller;

import com.exemplo.api.model.Cliente;
import com.exemplo.api.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Camada de Controle (CONTROLLER).
 * Ponto de entrada das requisições HTTP da API REST.
 * Responsável por receber parâmetros, delegar para a camada Service e retornar as respostas JSON (View).
 */
@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "*") // Permite acesso público aos parceiros da empresa
public class ClienteController {

    private final ClienteService clienteService;

    @Autowired
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * 1. Find All: Retorna todos os registros cadastrados.
     * GET /clientes
     */
    @GetMapping
    public List<Cliente> listarTodos() {
        return clienteService.listarTodos();
    }

    /**
     * 2. Find By ID: Retorna um registro específico com base no ID.
     * GET /clientes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 3. Find By Name: Retorna registros que correspondam a um nome específico.
     * GET /clientes/nome/{nome}
     */
    @GetMapping("/nome/{nome}")
    public List<Cliente> buscarPorNome(@PathVariable String nome) {
        return clienteService.buscarPorNome(nome);
    }

    /**
     * 4. Contagem: Endpoint para retornar o número total de registros.
     * GET /clientes/contar
     */
    @GetMapping("/contar")
    public long contarClientes() {
        return clienteService.contarClientes();
    }

    /**
     * 5. Create: Criação de um novo cliente.
     * POST /clientes
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente salvar(@Valid @RequestBody Cliente cliente) {
        return clienteService.salvar(cliente);
    }

    /**
     * 6. Update: Atualização de registro existente.
     * PUT /clientes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable Long id, @Valid @RequestBody Cliente cliente) {
        return clienteService.atualizar(id, cliente)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 7. Delete: Exclusão de registro por ID.
     * DELETE /clientes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        boolean deletado = clienteService.deletar(id);
        if (deletado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

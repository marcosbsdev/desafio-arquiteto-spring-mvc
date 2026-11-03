package com.exemplo.api.service;

import com.exemplo.api.model.Cliente;
import com.exemplo.api.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Camada de Serviço (SERVICE / BUSINESS LOGIC).
 * Centraliza as regras de negócio, transações e orquestra a comunicação entre Controller e Repository.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Retorna todos os clientes cadastrados.
     */
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    /**
     * Busca um cliente específico pelo ID.
     */
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    /**
     * Busca clientes por nome (busca flexível com fallback para exata).
     */
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorNome(String nome) {
        List<Cliente> clientes = clienteRepository.findByNomeContainingIgnoreCase(nome);
        if (clientes.isEmpty()) {
            return clienteRepository.findByNome(nome);
        }
        return clientes;
    }

    /**
     * Retorna a contagem total de clientes na base.
     */
    @Transactional(readOnly = true)
    public long contarClientes() {
        return clienteRepository.count();
    }

    /**
     * Cadastra um novo cliente no sistema.
     */
    @Transactional
    public Cliente salvar(Cliente cliente) {
        // Validação de e-mail duplicado
        if (cliente.getId() == null && clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new IllegalArgumentException("Já existe um cliente cadastrado com o e-mail: " + cliente.getEmail());
        }

        if (cliente.getStatus() == null || cliente.getStatus().isBlank()) {
            cliente.setStatus("ATIVO");
        }

        return clienteRepository.save(cliente);
    }

    /**
     * Atualiza os dados de um cliente existente.
     */
    @Transactional
    public Optional<Cliente> atualizar(Long id, Cliente clienteAtualizado) {
        return clienteRepository.findById(id).map(clienteExistente -> {
            clienteExistente.setNome(clienteAtualizado.getNome());
            clienteExistente.setEmail(clienteAtualizado.getEmail());
            clienteExistente.setTelefone(clienteAtualizado.getTelefone());
            if (clienteAtualizado.getStatus() != null && !clienteAtualizado.getStatus().isBlank()) {
                clienteExistente.setStatus(clienteAtualizado.getStatus());
            }
            return clienteRepository.save(clienteExistente);
        });
    }

    /**
     * Remove um cliente pelo ID.
     */
    @Transactional
    public boolean deletar(Long id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

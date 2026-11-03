package com.exemplo.api.repository;

import com.exemplo.api.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface de Repositório (Data Access Object / Repository Pattern).
 * Fornece métodos para interação com o banco de dados via Spring Data JPA.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca clientes com correspondência exata por nome.
     * Exigido no enunciado do desafio.
     */
    List<Cliente> findByNome(String nome);

    /**
     * Busca flexível por nome ignorando maiúsculas e minúsculas (contendo o termo).
     */
    List<Cliente> findByNomeContainingIgnoreCase(String nome);

    /**
     * Busca cliente por e-mail para validação de unicidade.
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Verifica se já existe cliente cadastrado com o e-mail informado.
     */
    boolean existsByEmail(String email);
}

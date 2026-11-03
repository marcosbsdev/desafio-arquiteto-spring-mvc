package com.exemplo.api.dto;

import com.exemplo.api.model.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) para Cliente.
 * Separa a representação de dados exposta externamente da entidade de banco de dados,
 * promovendo desacoplamento e encapsulamento na arquitetura MVC.
 */
public class ClienteDTO {

    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    private String email;

    private String telefone;
    private String status;

    public ClienteDTO() {
    }

    public ClienteDTO(Long id, String nome, String email, String telefone, String status) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.status = status;
    }

    public ClienteDTO(Cliente cliente) {
        if (cliente != null) {
            this.id = cliente.getId();
            this.nome = cliente.getNome();
            this.email = cliente.getEmail();
            this.telefone = cliente.getTelefone();
            this.status = cliente.getStatus();
        }
    }

    public Cliente toEntity() {
        return new Cliente(this.id, this.nome, this.email, this.telefone, this.status);
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

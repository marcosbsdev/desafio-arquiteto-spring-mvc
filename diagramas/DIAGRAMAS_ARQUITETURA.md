# Diagramas de Arquitetura de Software

Este documento apresenta a modelagem arquitetural completa da solução desenvolvida para disponibilizar dados de clientes aos parceiros de negócios, utilizando o padrão **MVC (Model-View-Controller)** sobre **Spring Boot (Java 17)** e **Spring Data JPA**.

---

## 1. C4 Model - Nível 1: Diagrama de Contexto (System Context)

Mostra os atores externos (Parceiros Comerciais) e como eles interagem com a API REST.

```mermaid
flowchart TD
    Parceiro["👤 Parceiro Comercial / Sistema B2B<br/>[Pessoa / Sistema Externo]<br/>Consome dados de clientes da plataforma"]
    Admin["👤 Administrador / Arquiteto<br/>[Pessoa]<br/>Gerencia os dados e monitora a API"]

    SistemaAPI["🌐 API REST de Clientes<br/>[Sistema de Software]<br/>Expõe endpoints RESTful para consulta, criação, atualização e exclusão de clientes"]

    BancoH2[("💾 Banco de Dados H2<br/>[Banco Relacional]<br/>Armazena os registros de clientes")]

    Parceiro -->|"Consome endpoints HTTP/JSON (GET, POST, PUT, DELETE)"| SistemaAPI
    Admin -->|"Consulta métricas e Console H2 (/h2-console)"| SistemaAPI
    SistemaAPI -->|"Lê e persiste dados via JDBC / JPA"| BancoH2

    classDef person fill:#08427b,stroke:#052e56,color:#ffffff;
    classDef system fill:#1168bd,stroke:#0b4884,color:#ffffff;
    classDef db fill:#2a7ab0,stroke:#1d557a,color:#ffffff;
    class Parceiro,Admin person;
    class SistemaAPI system;
    class BancoH2 db;
```

---

## 2. C4 Model - Nível 2: Diagrama de Contêineres (Container Diagram)

Detalha as tecnologias, protocolos e responsabilidades de cada contêiner de software.

```mermaid
flowchart TD
    subgraph FronteiraCliente["Fronteira Externa"]
        ClientApp["💻 Aplicação do Parceiro<br/>[Web / Mobile / Backend]<br/>Cliente HTTP"]
    end

    subgraph FronteiraBackend["Spring Boot Application (Porta 8080)"]
        TomcatServer["🖥️ Apache Tomcat Embutido<br/>[Web Server]<br/>Recebe conexões HTTP/1.1"]
        SpringMVC["⚙️ Spring MVC Framework<br/>[DispatcherServlet & Controllers]<br/>Roteamento REST e serialização JSON"]
        SpringDataJPA["🗄️ Spring Data JPA / Hibernate<br/>[ORM]<br/>Mapeamento Objeto-Relacional"]
    end

    subgraph Persistencia["Armazenamento"]
        H2MemDB[("💾 H2 In-Memory Database<br/>[JDBC: jdbc:h2:mem:clientesdb]<br/>Tabela: tb_clientes")]
    end

    ClientApp -->|"HTTPS / HTTP JSON<br/>(Porta 8080)"| TomcatServer
    TomcatServer -->|"Encaminha requisições"| SpringMVC
    SpringMVC -->|"Opera entidades de domínio"| SpringDataJPA
    SpringDataJPA -->|"SQL / Dialeto H2"| H2MemDB
```

---

## 3. C4 Model - Nível 3: Diagrama de Componentes (Padrão MVC)

Evidencia a divisão de responsabilidades estrita do padrão arquitetural **MVC**:

```mermaid
flowchart TD
    subgraph CamadaView["VIEW (Representação REST)"]
        JSONPayload["📄 Resposta / Requisição JSON<br/>[DTO / Serialização Jackson]<br/>Representação consumida pelos clientes"]
    end

    subgraph CamadaController["CONTROLLER (Controle de Fluxo)"]
        ClienteController["🎮 ClienteController<br/>[@RestController /clientes]<br/>• listarTodos()<br/>• buscarPorId()<br/>• buscarPorNome()<br/>• contarClientes()<br/>• salvar()<br/>• atualizar()<br/>• deletar()"]
    end

    subgraph CamadaService["SERVICE (Regras de Negócio)"]
        ClienteService["💼 ClienteService<br/>[@Service]<br/>Validações de unicidade, transações (@Transactional) e lógica de negócio"]
    end

    subgraph CamadaModel["MODEL & PERSISTÊNCIA (Dados)"]
        ClienteRepository["📦 ClienteRepository<br/>[JpaRepository interface]<br/>findByNome(), count(), etc."]
        ClienteEntity["🏛️ Cliente (Entity)<br/>[@Entity tb_clientes]<br/>id, nome, email, telefone, status"]
    end

    JSONPayload <-->|"1. Requisição / Resposta HTTP"| ClienteController
    ClienteController -->|"2. Delega processamento"| ClienteService
    ClienteService -->|"3. Executa queries e persistência"| ClienteRepository
    ClienteRepository -->|"4. Mapeia estado"| ClienteEntity
```

---

## 4. Diagrama de Classes UML

Apresenta a estrutura de classes, tipagem, métodos e associações do projeto:

```mermaid
classDiagram
    direction TB

    class Cliente {
        -Long id
        -String nome
        -String email
        -String telefone
        -String status
        +getId() Long
        +setId(Long id) void
        +getNome() String
        +setNome(String nome) void
        +getEmail() String
        +setEmail(String email) void
        +getTelefone() String
        +setTelefone(String telefone) void
        +getStatus() String
        +setStatus(String status) void
    }

    class ClienteDTO {
        -Long id
        -String nome
        -String email
        -String telefone
        -String status
        +toEntity() Cliente
    }

    class ClienteRepository {
        <<interface>>
        +findByNome(String nome) List~Cliente~
        +findByNomeContainingIgnoreCase(String nome) List~Cliente~
        +findByEmail(String email) Optional~Cliente~
        +existsByEmail(String email) boolean
        +findAll() List~Cliente~
        +findById(Long id) Optional~Cliente~
        +count() long
        +save(Cliente c) Cliente
        +deleteById(Long id) void
    }

    class ClienteService {
        -ClienteRepository clienteRepository
        +listarTodos() List~Cliente~
        +buscarPorId(Long id) Optional~Cliente~
        +buscarPorNome(String nome) List~Cliente~
        +contarClientes() long
        +salvar(Cliente cliente) Cliente
        +atualizar(Long id, Cliente cliente) Optional~Cliente~
        +deletar(Long id) boolean
    }

    class ClienteController {
        -ClienteService clienteService
        +listarTodos() List~Cliente~
        +buscarPorId(Long id) ResponseEntity~Cliente~
        +buscarPorNome(String nome) List~Cliente~
        +contarClientes() long
        +salvar(Cliente cliente) Cliente
        +atualizar(Long id, Cliente cliente) ResponseEntity~Cliente~
        +deletar(Long id) ResponseEntity~Void~
    }

    ClienteController --> ClienteService : Injeta (@Autowired)
    ClienteService --> ClienteRepository : Injeta (@Autowired)
    ClienteRepository ..> Cliente : Gerencia persistência
    ClienteDTO ..> Cliente : Mapeia dados
```

---

## 5. Diagrama de Sequência UML (Ciclo de Vida de uma Requisição REST)

Exemplo: Cadastro de um novo cliente (`POST /clientes`).

```mermaid
sequenceDiagram
    autonumber
    actor Parceiro as Parceiro (Cliente HTTP)
    participant Controller as ClienteController
    participant Service as ClienteService
    participant Repo as ClienteRepository
    participant DB as Banco H2

    Parceiro->>Controller: POST /clientes (JSON: nome, email, telefone)
    activate Controller
    Note over Controller: Validação (@Valid)
    Controller->>Service: salvar(cliente)
    activate Service
    
    Service->>Repo: existsByEmail(cliente.getEmail())
    activate Repo
    Repo->>DB: SELECT count(1) FROM tb_clientes WHERE email = ?
    activate DB
    DB-->>Repo: 0 (Não existe)
    deactivate DB
    Repo-->>Service: false
    deactivate Repo

    Service->>Repo: save(cliente)
    activate Repo
    Repo->>DB: INSERT INTO tb_clientes (nome, email, telefone, status) VALUES (...)
    activate DB
    DB-->>Repo: Registro inserido (id = 5)
    deactivate DB
    Repo-->>Service: Cliente salvo (id = 5)
    deactivate Repo

    Service-->>Controller: Cliente
    deactivate Service
    Controller-->>Parceiro: 201 Created (JSON com ID gerado)
    deactivate Controller
```

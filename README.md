# Bootcamp Arquiteto(a) de Software — Desafio Final
## API RESTful no Padrão Arquitetural MVC com Spring Boot e H2 Database

Projeto desenvolvido como solução do **Desafio Final do Bootcamp de Arquiteto(a) de Software**, cujo objetivo é projetar, documentar e implantar uma API RESTful de alta qualidade para disponibilizar dados cadastrais de clientes aos parceiros de negócios de uma grande empresa de vendas on-line.

---

## 📋 Sumário
1. [Visão Geral da Solução](#-visão-geral-da-solução)
2. [Estrutura de Pastas e Componentes](#-estrutura-de-pastas-e-componentes)
3. [Padrão Arquitetural MVC Aplicado à API REST](#-padrão-arquitetural-mvc-aplicado-à-api-rest)
4. [Persistência de Dados e Banco H2](#-persistência-de-dados-e-banco-h2)
5. [Endpoints da API](#-endpoints-da-api)
6. [Diagramas Arquiteturais](#-diagramas-arquiteturais)
7. [Como Executar o Projeto](#-como-executar-o-projeto)
8. [Exemplos de Requisições (cURL)](#-exemplos-de-requisições-curl)

---

## 🚀 Visão Geral da Solução

- **Linguagem & Plataforma:** Java 17 + Spring Boot 3
- **Estilo Arquitetural:** RESTful API
- **Padrão de Projeto:** MVC (Model-View-Controller) com Camadas de Serviço e Repositório (Repository Pattern)
- **Persistência Transacional:** Spring Data JPA + Hibernate + **PostgreSQL 16** (com perfil alternativo H2)
- **Migrations de Banco de Dados:** **Flyway** (`V1__criar_tabela_clientes.sql`, `V2__inserir_clientes_iniciais.sql`)
- **Conteinerização:** **Docker** (`Dockerfile` Multi-Stage Build) & **Docker Compose** (`docker-compose.yml`)
- **Validação:** Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Email`)
- **Segurança & CORS:** `@CrossOrigin(origins = "*")` configurado para disponibilização pública aos parceiros

---

## 📁 Estrutura de Pastas e Componentes

```text
desafio-arquiteto-spring-mvc/
├── Dockerfile                               # Build Multi-Stage (Maven JDK 17 -> JRE 17 Alpine)
├── docker-compose.yml                       # Orquestração da API (8080) + PostgreSQL 16 (5435)
├── .dockerignore                            # Exclusões de contexto de build Docker
├── pom.xml                                  # Gerenciamento de dependências Maven
├── requisicoes.http                         # Arquivo para testes de requisições HTTP (REST Client / IntelliJ)
├── README.md                                # Documentação completa do projeto
├── diagramas/
│   ├── DIAGRAMAS_ARQUITETURA.md             # Diagramas C4 Model, UML e Sequência em Mermaid
│   └── arquitetura-drawio.xml               # Arquivo nativo para importação no Draw.io / diagrams.net
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── exemplo/
        │           └── api/
        │               ├── ApiApplication.java               # Ponto de entrada (Bootstrap do Spring Boot)
        │               ├── controller/
        │               │   └── ClienteController.java        # Controlador REST (Camada CONTROLLER)
        │               ├── service/
        │               │   └── ClienteService.java           # Lógica de negócio (Camada SERVICE)
        │               ├── repository/
        │               │   └── ClienteRepository.java        # Acesso ao banco de dados (Camada REPOSITORY)
        │               ├── model/
        │               │   └── Cliente.java                  # Entidade JPA (Camada MODEL)
        │               ├── dto/
        │               │   └── ClienteDTO.java               # Objeto de transferência de dados
        │               └── config/
        │                   ├── CarregadorDadosIniciais.java  # Fallback de carga inicial de dados
        │                   └── GlobalExceptionHandler.java   # Padronização de mensagens de erro JSON
        └── resources/
            ├── application.properties       # Configurações do PostgreSQL, Flyway, JPA e porta 8080
            ├── application-h2.properties    # Perfil opcional em memória (--spring.profiles.active=h2)
            └── db/
                └── migration/
                    ├── V1__criar_tabela_clientes.sql         # Migration 1: DDL da tabela tb_clientes e índices
                    └── V2__inserir_clientes_iniciais.sql     # Migration 2: Carga inicial de clientes
```

### Explicação do Papel de Cada Componente:

1. **`controller/ClienteController.java` (CONTROLLER):**
   - Intercepta as requisições HTTP (`GET`, `POST`, `PUT`, `DELETE`).
   - Valida os parâmetros de entrada (`@Valid`, `@PathVariable`, `@RequestBody`).
   - Delega a execução para o `ClienteService`.
   - Produz a **VIEW** no contexto de APIs modernas: serializa o resultado em JSON com os devidos códigos de status HTTP (`200 OK`, `201 Created`, `204 No Content`, `404 Not Found`).

2. **`service/ClienteService.java` (SERVICE / LÓGICA DE NEGÓCIO):**
   - Isola as regras de negócio e validações lógicas (ex.: garantia de não duplicidade de e-mail).
   - Controla a fronteira transacional da aplicação (`@Transactional`).
   - Garante o desacoplamento entre os detalhes de transporte HTTP e o acesso a dados.

3. **`repository/ClienteRepository.java` (REPOSITORY / DATA ACCESS):**
   - Estende `JpaRepository<Cliente, Long>`, herdando operações padrão (`findAll`, `findById`, `save`, `deleteById`, `count`).
   - Define consultas derivadas customizadas, como `findByNome(String nome)` e `findByNomeContainingIgnoreCase(String nome)`.

4. **`model/Cliente.java` (MODEL / ENTIDADE DE DOMÍNIO):**
   - Modela os atributos do domínio de clientes: `id`, `nome`, `email`, `telefone`, `status`.
   - Utiliza anotações JPA (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`) para mapeamento objeto-relacional na tabela `tb_clientes`.

5. **`dto/ClienteDTO.java` (DATA TRANSFER OBJECT):**
   - Evita o acoplamento direto de contratos externos com o schema do banco de dados, permitindo evolução independente da API.

6. **`config/CarregadorDadosIniciais.java` (CARGA INICIAL):**
   - Implementa `CommandLineRunner` para inserir 4 clientes fictícios na inicialização, permitindo que a API já seja testada imediatamente sem necessidade de cadastro prévio.

7. **`config/GlobalExceptionHandler.java` (TRATAMENTO DE ERROS):**
   - Intercepta falhas de validação de dados e conflitos de regras de negócio, retornando JSONs claros e semânticos (`400 Bad Request`, `409 Conflict`).

---

## 🏛️ Padrão Arquitetural MVC Aplicado à API REST

Em aplicações web tradicionais, o padrão **MVC (Model-View-Controller)** renderiza telas HTML no servidor. Em arquiteturas contemporâneas de **APIs RESTful orientadas a microsserviços e integração B2B**, o padrão se traduz da seguinte maneira:

| Componente MVC | Implementação no Projeto | Responsabilidade |
| :--- | :--- | :--- |
| **Model** | `Cliente.java` & `ClienteRepository.java` | Gerencia o estado, os dados e a persistência relacional. |
| **View** | Cargas úteis JSON (`ResponseEntity<T>`, DTOs) | Representação estruturada em JSON devolvida via protocolo HTTP ao consumidor. |
| **Controller** | `ClienteController.java` | Recebe a chamada HTTP, orquestra o fluxo de dados e seleciona a representação da View. |

---

## 💾 Persistência Transacional (PostgreSQL) e Migrations (Flyway)

A persistência de dados utiliza o **PostgreSQL 16** gerenciado com versionamento de schema via **Flyway Migrations**:

- **URL de Conexão (Local/Host):** `jdbc:postgresql://localhost:5435/clientesdb`
- **URL de Conexão (Rede Docker):** `jdbc:postgresql://postgres:5432/clientesdb`
- **Usuário:** `postgres` | **Senha:** `postgres`
- **Tabela Gerada:** `tb_clientes` (validada pelo Hibernate com `ddl-auto=validate`)
- **Migrations executadas automaticamente (`src/main/resources/db/migration`):**
  - `V1__criar_tabela_clientes.sql`: Criação da tabela `tb_clientes`, chave única de e-mail e índices de busca.
  - `V2__inserir_clientes_iniciais.sql`: Inserção inicial dos 4 clientes de demonstração.

---

## 📡 Endpoints da API

A API atende com precisão a todos os métodos exigidos no enunciado do desafio:

| Método | Endpoint | Descrição / Requisito | Status de Sucesso |
| :---: | :--- | :--- | :---: |
| `GET` | `/clientes/contar` | **Contagem:** Retorna o número total de registros | `200 OK` |
| `GET` | `/clientes` | **Find All:** Retorna todos os clientes | `200 OK` |
| `GET` | `/clientes/{id}` | **Find By ID:** Retorna um cliente específico por ID | `200 OK` (ou `404`) |
| `GET` | `/clientes/nome/{nome}` | **Find By Name:** Retorna clientes por nome | `200 OK` |
| `POST` | `/clientes` | **Create:** Cadastra um novo cliente | `201 Created` |
| `PUT` | `/clientes/{id}` | **Update:** Atualiza os dados de um cliente existente | `200 OK` (ou `404`) |
| `DELETE`| `/clientes/{id}` | **Delete:** Remove um cliente por ID | `204 No Content` |

---

## 📊 Diagramas Arquiteturais

Os diagramas completos estão disponíveis em formato Mermaid no arquivo [`diagramas/DIAGRAMAS_ARQUITETURA.md`](diagramas/DIAGRAMAS_ARQUITETURA.md), englobando:
1. **C4 Model Nível 1 — Contexto de Sistema**
2. **C4 Model Nível 2 — Contêineres**
3. **C4 Model Nível 3 — Componentes MVC**
4. **Diagrama de Classes UML**
5. **Diagrama de Sequência de Requisição REST**

### Importando no Draw.io:
1. Acesse [app.diagrams.net](https://app.diagrams.net/).
2. Clique em **File** > **Open From** > **Device...** (ou arraste o arquivo).
3. Selecione o arquivo [`diagramas/arquitetura-drawio.xml`](diagramas/arquitetura-drawio.xml).
4. O diagrama visual completo estará pronto para edição e exportação em PNG ou PDF.

---

## 🛠️ Como Executar o Projeto

### Opção 1: Execução Completa via Docker Compose (Recomendado)
Sobe automaticamente o banco **PostgreSQL 16** (`porta 5435`) e a **API Spring Boot** (`porta 8080`), aplicando as migrations do Flyway:
```bash
docker compose up -d --build
```

### Opção 2: Execução Local (VS Code / IntelliJ / Maven) com PostgreSQL no Docker
1. Suba apenas o banco PostgreSQL:
   ```bash
   docker compose up -d postgres
   ```
2. Execute a aplicação via IDE (`ApiApplication.java`) ou Maven:
   ```bash
   mvn spring-boot:run
   ```

A aplicação responderá em `http://localhost:8080/clientes`.

---

## 🧪 Exemplos de Requisições (cURL)

Você pode testar rapidamente todos os endpoints utilizando os comandos abaixo no terminal:

### 1. Contagem total de clientes:
```bash
curl -X GET http://localhost:8080/clientes/contar
```

### 2. Listar todos os clientes (Find All):
```bash
curl -X GET http://localhost:8080/clientes
```

### 3. Buscar cliente por ID:
```bash
curl -X GET http://localhost:8080/clientes/1
```

### 4. Buscar cliente por Nome:
```bash
curl -X GET http://localhost:8080/clientes/nome/TechCorp
```

### 5. Cadastrar novo cliente (Create):
```bash
curl -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d "{\"nome\":\"Novo Parceiro Comercial\",\"email\":\"novo@parceiro.com\",\"telefone\":\"(11) 91234-5678\",\"status\":\"ATIVO\"}"
```

### 6. Atualizar cliente existente (Update):
```bash
curl -X PUT http://localhost:8080/clientes/1 \
  -H "Content-Type: application/json" \
  -d "{\"nome\":\"TechCorp Brasil Atualizada\",\"email\":\"contato@techcorp.com.br\",\"telefone\":\"(11) 98765-0000\",\"status\":\"ATIVO\"}"
```

### 7. Excluir cliente por ID (Delete):
```bash
curl -X DELETE http://localhost:8080/clientes/4
```

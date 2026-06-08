# BankAPI

Sistema bancário REST completo construído com **Spring Boot 3**, **Java 21**, **JWT** e deploy na nuvem via **Render**. Inclui gerenciamento de usuários, contas bancárias, transferências, PIX e autenticação segura.

![CI](https://github.com/RabeloDev7/bankapi/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![Deploy](https://img.shields.io/badge/Deploy-Render-blue)
![Status](https://img.shields.io/badge/status-online-success)

🌐 **Live:** [https://bankapi-gnf1.onrender.com](https://bankapi-gnf1.onrender.com)

---

## Funcionalidades

- Cadastro e autenticação de usuários com **JWT**
- Abertura e gerenciamento de contas bancárias
- Depósito, saque e transferência entre contas
- **PIX** — cadastro de chaves (EMAIL, CPF, PHONE, RANDOM) e envio instantâneo
- Histórico completo de transações por conta
- Frontend estático integrado (login, cadastro, dashboard, PIX, transferência, extrato)
- Deploy containerizado via **Docker** no Render
- CI/CD automático com **GitHub Actions**

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| Segurança | Spring Security + JWT (JJWT 0.12) |
| Persistência | Spring Data JPA + Hibernate 6 |
| Banco (produção) | PostgreSQL 18 (Render) |
| Banco (local/testes) | H2 in-memory |
| Build | Maven 3.9 |
| Boilerplate | Lombok |
| Testes | JUnit 5 + Mockito |
| CI/CD | GitHub Actions |
| Deploy | Docker + Render |

---

## Arquitetura

```
Controller → Service → Repository
    ↕            ↕
   DTO         Entity
```

- **Controllers** — entrada HTTP, validação via `@Valid`, delegação ao Service
- **Services** — regras de negócio, transações `@Transactional`
- **Repositories** — acesso a dados via Spring Data JPA
- **DTOs** — isolam a API das entidades JPA (`@Value` para responses, `@Data` para requests)
- **Exceptions** — `BusinessException` → 422, `ResourceNotFoundException` → 404, `BadCredentialsException` → 401
- **Auth** — `JwtUtil`, `JwtFilter`, `UserDetailsServiceImpl`, `SecurityConfig` STATELESS

---

## Pré-requisitos

- Java 21+
- Maven 3.9+

> Não é necessário instalar MySQL. O projeto usa **H2 in-memory** para desenvolvimento local.

---

## Rodando localmente

**1. Clone o repositório**
```bash
git clone https://github.com/RabeloDev7/bankapi.git
cd bankapi/bankapi
```

**2. Execute**
```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080` usando H2 in-memory automaticamente.

**Console H2** (visualizar tabelas em desenvolvimento):
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:bankapi_dev
User: sa  |  Password: (vazio)
```

---

## Frontend

Acesse `http://localhost:8080` — redireciona automaticamente para a tela de login.

| Página | Rota |
|---|---|
| Login | `/bankapi/pages/login.html` |
| Cadastro | `/bankapi/pages/register.html` |
| Dashboard | `/bankapi/pages/dashboard.html` |
| Contas / Extrato | `/bankapi/pages/accounts.html` |
| Transferência | `/bankapi/pages/transfer.html` |
| PIX | `/bankapi/pages/pix.html` |

---

## Endpoints da API

### Status
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `GET` | `/` | Redireciona para o frontend | Não |
| `GET` | `/api` | Status da API (JSON) | Não |
| `GET` | `/actuator/health` | Health check | Não |

### Auth
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `POST` | `/auth/login` | Login — retorna JWT | Não |

### Usuários
| Método | Rota | Body | Auth |
|---|---|---|---|
| `POST` | `/users` | `{ name, email, password }` | Não |
| `GET` | `/users` | — | Sim |
| `GET` | `/users/{id}` | — | Sim |
| `PUT` | `/users/{id}` | `{ name, email, password }` | Sim |
| `DELETE` | `/users/{id}` | — | Sim |

### Contas
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `POST` | `/accounts?userId=` | Abrir conta | Sim |
| `GET` | `/accounts/{id}` | Buscar conta | Sim |
| `GET` | `/accounts/user/{userId}` | Contas do usuário | Sim |
| `DELETE` | `/accounts/{id}` | Encerrar conta | Sim |

### Operações
| Método | Rota | Body | Auth |
|---|---|---|---|
| `POST` | `/accounts/{id}/deposit` | `{ amount, description }` | Sim |
| `POST` | `/accounts/{id}/withdraw` | `{ amount, description }` | Sim |
| `POST` | `/accounts/{id}/transfer` | `{ toAccountId, amount, description }` | Sim |
| `GET` | `/accounts/{id}/transactions` | — | Sim |

### PIX
| Método | Rota | Body | Auth |
|---|---|---|---|
| `POST` | `/pix/keys` | `{ type, keyValue, accountId }` | Sim |
| `GET` | `/pix/keys/account/{accountId}` | — | Sim |
| `DELETE` | `/pix/keys/{keyId}` | — | Sim |
| `POST` | `/pix/send/{fromAccountId}` | `{ pixKey, amount, description }` | Sim |

---

## Exemplos de uso (curl)

**Cadastrar usuário**
```bash
curl -X POST https://bankapi-gnf1.onrender.com/users \
  -H "Content-Type: application/json" \
  -d '{"name":"João","email":"joao@email.com","password":"123456"}'
```

**Login**
```bash
curl -X POST https://bankapi-gnf1.onrender.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@email.com","password":"123456"}'
```

**Abrir conta**
```bash
curl -X POST "https://bankapi-gnf1.onrender.com/accounts?userId=1" \
  -H "Authorization: Bearer SEU_TOKEN"
```

**Depositar**
```bash
curl -X POST https://bankapi-gnf1.onrender.com/accounts/ID_DA_CONTA/deposit \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":500.00,"description":"Deposito inicial"}'
```

**Cadastrar chave PIX**
```bash
curl -X POST https://bankapi-gnf1.onrender.com/pix/keys \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"EMAIL","keyValue":"joao@email.com","accountId":"ID_DA_CONTA"}'
```

**Enviar PIX**
```bash
curl -X POST https://bankapi-gnf1.onrender.com/pix/send/ID_DA_CONTA_ORIGEM \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pixKey":"destino@email.com","amount":100.00,"description":"Aluguel"}'
```

---

## Rodando os testes

```bash
mvn test
```

Os testes usam H2 in-memory — não é necessário nenhum banco externo.

Cobertura atual: **22 testes unitários** nos services `UserService`, `TransactionService` e `PixService`.

---

## Estrutura do projeto

```
bankapi/
├── Dockerfile                          # Build multi-stage (Maven + JRE Alpine)
├── render.yaml                         # Infraestrutura como código (Render)
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/leonardo/bankapi/
    │   │   ├── auth/          # JwtUtil, JwtFilter, AuthService, AuthController
    │   │   ├── config/        # SecurityConfig, DataSourceConfig (converte DATABASE_URL)
    │   │   ├── controller/    # UserController, AccountController, PixController, HomeController
    │   │   ├── dto/           # Request/Response DTOs (Lombok @Value / @Data)
    │   │   ├── entity/        # User, Account, Transaction, PixKey, PixKeyType
    │   │   ├── exception/     # BusinessException, ResourceNotFoundException, GlobalExceptionHandler
    │   │   ├── repository/    # JPA Repositories
    │   │   └── service/       # UserService, AccountService, TransactionService, PixService
    │   └── resources/
    │       ├── application.properties           # Perfil local (H2)
    │       ├── application-prod.properties      # Perfil produção (PostgreSQL/Render)
    │       └── static/bankapi/pages/            # Frontend HTML
    └── test/
        ├── java/.../service/                    # UserServiceTest, TransactionServiceTest, PixServiceTest
        └── resources/application-test.properties
```

---

## Deploy (Render)

O projeto faz deploy automático no Render a cada push na branch `main`.

### Variáveis de ambiente (configuradas no Render)

| Variável | Descrição |
|---|---|
| `DATABASE_URL` | URL do PostgreSQL fornecida pelo Render (`postgresql://...`) |
| `JWT_SECRET` | Chave secreta JWT (mínimo 32 caracteres) |
| `JWT_EXPIRATION` | Expiração do token em ms (padrão: `86400000` = 24h) |
| `SPRING_PROFILES_ACTIVE` | Deve ser `prod` |
| `PORT` | Porta injetada automaticamente pelo Render (padrão `10000`) |

### Como funciona o deploy

1. Push para `main` dispara o GitHub Actions (build + testes com H2)
2. Render detecta o novo commit e inicia o build Docker
3. O `Dockerfile` usa build multi-stage: compila com Maven e roda com JRE Alpine
4. `DataSourceConfig.java` converte `DATABASE_URL` (`postgresql://`) para o formato JDBC automaticamente
5. Hibernate cria/atualiza as tabelas via `ddl-auto=update`

> **Nota:** No plano gratuito do Render, o serviço hiberna após 15 minutos de inatividade. A primeira requisição pode demorar ~30 segundos para "acordar".

---

## Autor

**Leonardo Rabelo**
Desenvolvido como projeto de portfólio — todos os direitos reservados.

[![GitHub](https://img.shields.io/badge/GitHub-RabeloDev7-black?logo=github)](https://github.com/RabeloDev7)

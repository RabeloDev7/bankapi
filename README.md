# BankAPI

Sistema bancário REST construído com **Spring Boot 3**, **Java 21** e **JWT**. Inclui gerenciamento de usuários, contas, transferências, PIX e autenticação segura.

![CI](https://github.com/RabeloDev7/bankapi/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)

---

## Funcionalidades

- Cadastro e autenticação de usuários (JWT)
- Abertura e gerenciamento de contas bancárias
- Depósito, saque e transferência entre contas
- PIX — cadastro de chaves (EMAIL, CPF, PHONE, RANDOM) e envio instantâneo
- Histórico de transações por conta
- Frontend estático integrado (login, dashboard, PIX, transferência)

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| Segurança | Spring Security + JWT (JJWT 0.12) |
| Persistência | Spring Data JPA + Hibernate |
| Banco (produção) | MySQL 8 |
| Banco (testes) | H2 in-memory |
| Build | Maven |
| Boilerplate | Lombok |
| Testes | JUnit 5 + Mockito |
| CI | GitHub Actions |

---

## Arquitetura

```
Controller → Service → Repository
    ↕            ↕
   DTO         Entity
```

- **Controllers** — entrada HTTP, validação de request, delegação ao Service
- **Services** — regras de negócio, transações `@Transactional`
- **Repositories** — acesso a dados via Spring Data JPA
- **DTOs** — isolam a API das entidades JPA
- **Exceptions** — `BusinessException` (422), `ResourceNotFoundException` (404)

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- MySQL 8+ rodando localmente

---

## Configuração

**1. Clone o repositório**
```bash
git clone https://github.com/RabeloDev7/bankapi.git
cd bankapi/bankapi
```

**2. Crie o banco de dados**
```sql
CREATE DATABASE bank_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**3. Configure as credenciais**

Edite `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/bank_app
spring.datasource.username=root
spring.datasource.password=SUA_SENHA
```

> Nunca commite senhas reais. Use variáveis de ambiente em produção:
> ```bash
> export SPRING_DATASOURCE_PASSWORD=sua_senha
> ```

**4. Execute**
```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

---

## Frontend

Acesse `http://localhost:8080/bankapi/pages/login.html` para usar o frontend integrado.

Fluxo:
1. Crie uma conta em `/register.html`
2. Faça login — o JWT é salvo no `localStorage`
3. Dashboard exibe saldo e transações em tempo real
4. PIX — cadastre chaves e envie pagamentos

---

## Endpoints da API

### Auth
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `POST` | `/auth/login` | Login — retorna JWT | Não |

### Usuários
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `POST` | `/users` | Cadastrar usuário | Não |
| `GET` | `/users` | Listar usuários | Sim |
| `GET` | `/users/{id}` | Buscar por ID | Sim |
| `PUT` | `/users/{id}` | Atualizar usuário | Sim |
| `DELETE` | `/users/{id}` | Remover usuário | Sim |

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
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"João","email":"joao@email.com","password":"123456"}'
```

**Login**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@email.com","password":"123456"}'
```

**Abrir conta**
```bash
curl -X POST "http://localhost:8080/accounts?userId=1" \
  -H "Authorization: Bearer SEU_TOKEN"
```

**Depositar**
```bash
curl -X POST http://localhost:8080/accounts/ID_DA_CONTA/deposit \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":500.00,"description":"Depósito inicial"}'
```

**Cadastrar chave PIX**
```bash
curl -X POST http://localhost:8080/pix/keys \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"EMAIL","keyValue":"joao@email.com","accountId":"ID_DA_CONTA"}'
```

**Enviar PIX**
```bash
curl -X POST http://localhost:8080/pix/send/ID_DA_CONTA_ORIGEM \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pixKey":"destino@email.com","amount":100.00,"description":"Aluguel"}'
```

---

## Rodando os testes

```bash
mvn test
```

Os testes usam H2 in-memory — não é necessário MySQL para rodar a suíte.

Cobertura atual: **22 testes unitários** nos services `UserService`, `TransactionService` e `PixService`.

---

## Estrutura do projeto

```
src/
├── main/java/com/leonardo/bankapi/
│   ├── auth/           # JWT: JwtUtil, JwtFilter, AuthService, AuthController
│   ├── config/         # SecurityConfig
│   ├── controller/     # UserController, AccountController, PixController
│   ├── dto/            # Request/Response DTOs
│   ├── entity/         # User, Account, Transaction, PixKey, PixKeyType
│   ├── exception/      # BusinessException, ResourceNotFoundException, GlobalExceptionHandler
│   ├── repository/     # JPA Repositories
│   └── service/        # UserService, AccountService, TransactionService, PixService
├── main/resources/
│   ├── application.properties
│   └── static/bankapi/pages/  # Frontend HTML
└── test/
    ├── java/.../service/       # UserServiceTest, TransactionServiceTest, PixServiceTest
    └── resources/application-test.properties  # H2 config
```

---

## Variáveis de ambiente (produção)

| Variável | Descrição |
|---|---|
| `SPRING_DATASOURCE_URL` | URL do MySQL |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Chave secreta JWT (mín. 32 chars) |
| `JWT_EXPIRATION` | Expiração em ms (padrão: 86400000 = 24h) |

---

## Contribuindo

1. Fork o repositório
2. Crie uma branch: `git checkout -b feature/minha-feature`
3. Commit: `git commit -m "feat: descrição da mudança"`
4. Push: `git push origin feature/minha-feature`
5. Abra um Pull Request

---

## Licença

MIT — sinta-se livre para usar, modificar e distribuir.

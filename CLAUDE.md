# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

FitZone is a Spring Boot REST API simulating gym/studio management: students (`Aluno`), reservable environments (`Ambiente` — weight room, yoga, crossfit, pool), bookings (`Agendamento`) with add-on services (`ServicoAdicional` — physical assessment, nutritionist, personal trainer, locker), and occupancy/revenue reports. It began as a pure-Java OOP exercise and evolved in phases (documented in [README.md](README.md)) into a layered Spring Boot app backed by MySQL; it is currently web-only (no console UI, no `Main.java`).

## Commands

Requires JDK 21, Maven, and a local MySQL with the `fitzone` database created.

```bash
# create/update schema (run once, or whenever sql/schema.sql changes)
mysql -u SEU_USUARIO -p fitzone < sql/schema.sql

# set DB password (PowerShell) — never hardcode it in application.properties
$env:DB_PASSWORD = "sua_senha_do_mysql"

# run the API (http://localhost:8080)
mvn spring-boot:run

# run all tests
mvn test

# run a single test class
mvn test -Dtest=AlunoServiceTest

# run a single test method
mvn test -Dtest=AlunoServiceTest#deveCadastrarAlunoComSucesso
```

**Tests are integration tests against a real MySQL database** (schema `fitzone`, same one used in development), not mocked — `@SpringBootTest` boots the full Spring context and each test class truncates the relevant tables in `@BeforeEach`/`@AfterAll`. `DB_PASSWORD` must be set and MySQL must be running before `mvn test` will pass. There is no in-memory/H2 test profile — this was a deliberate choice recorded in the README, not an oversight.

`spring.jpa.hibernate.ddl-auto=validate` — Hibernate never auto-generates schema. Any entity/column change must be reflected by hand in [sql/schema.sql](sql/schema.sql) and re-applied to the database, or the app fails to start.

## Architecture

Standard layered structure, each package with a single responsibility:

```
app/         FitzoneApplication — Spring Boot entry point
entidades/   JPA-annotated domain model
repository/  Spring Data JPA interfaces
service/     business logic (@Service, one per resource/context)
controller/  REST layer (@RestController, one per resource)
dto/         request/response records — never expose entities directly over JSON
web/         @RestControllerAdvice (ApiExceptionHandler) + ApiErrorResponse
excecoes/    custom domain exceptions
sql/         schema.sql — hand-maintained MySQL DDL (source of truth for the schema)
```

Resources and their REST routes: `/api/alunos`, `/api/ambientes`, `/api/agendamentos` (+ `POST /api/agendamentos/{id}/servicos`), `/api/relatorios/{alunos/{cpf}, ambientes, faturamento, servicos}`.

### Inheritance mapping (SINGLE_TABLE)

Both `Ambiente` (abstract) and `ServicoAdicional` (abstract) use JPA `InheritanceType.SINGLE_TABLE` with a `tipo` discriminator column — all subtypes live in one table (`ambientes`, `servicos_adicionais`). Each subtype overrides `getDescricao()`/`getValorTotal()` (`ServicoAdicional`) or `getDescricao()`/`getTipo()` (`Ambiente`) to keep real polymorphism despite the flattened table. `ServicoAdicional` was an interface in the original Java-only phase; it became an abstract `@Entity` because JPA cannot map interfaces — that decision is intentional, not a code smell.

`Ambiente.id` is a manually-assigned `String` (not auto-increment); `Agendamento.id` and `ServicoAdicional.id` are DB `IDENTITY` (`AUTO_INCREMENT`).

### DTOs are mandatory at the API boundary

`Agendamento` has EAGER fetches and a bidirectional relation with `ServicoAdicional` that would recurse infinitely under default Jackson serialization. Controllers must always convert to/from `dto/*` records — never return or accept JPA entities directly in a controller method.

### Business rules to preserve

- `Agendamento.calculaValorTotal()`: duration is floored to a **minimum of 1 hour** (`horas <= 0 → horas = 1`) before multiplying by `ambiente.getValorHora()`, then add-on service totals are summed in. Any report or calculation touching booking duration/value must apply the same floor — a prior bug (`LeitorEntrada.lerHoraApos()` had the start/end validation inverted) caused negative report values specifically because this floor wasn't applied consistently.
- Time-conflict checking is done in the database, not in memory: `AgendamentoRepository.buscarConflitos()` is a JPQL query filtering by ambiente/date/time overlap directly, avoiding a full table scan.
- `ApiExceptionHandler` maps domain exceptions to HTTP status by category: not-found exceptions → 404, "already exists"/availability/limit exceptions → 409, `ServicoInvalidoException` and Bean Validation failures → 400, `FalhaPersistenciaException` → 500. When adding a new domain exception, add it to the matching `@ExceptionHandler` group rather than creating a new response shape.

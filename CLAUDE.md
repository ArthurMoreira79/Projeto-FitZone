# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

FitZone is a Spring Boot REST API simulating gym/studio management: students (`Aluno`), reservable environments (`Ambiente` — weight room, yoga, crossfit, pool), bookings (`Agendamento`) with add-on services (`ServicoAdicional` — physical assessment, nutritionist, personal trainer, locker), and occupancy/revenue reports. It began as a pure-Java OOP console exercise and evolved in phases (documented in [README.md](README.md)); it is now web-only (no console UI, no `Main.java`). The developer is learning Spring Boot with this project and writes the code themselves — explain decisions and send code in chat rather than editing files, unless explicitly asked to change files. Respond in Portuguese.

## Commands

Requires JDK 21 and a local MySQL 8.0.16+ (needed for `CHECK` constraints to be enforced). Maven is provided by the wrapper — use `mvnw`, not a global `mvn`. Run everything from the project root (the `.env` is resolved relative to the working directory).

```bash
# run the API (http://localhost:8080/api) — Flyway applies pending migrations on startup
.\mvnw.cmd spring-boot:run

# run all tests (uses the fitzone_test database, created automatically)
.\mvnw.cmd test

# run a single test class / method
.\mvnw.cmd test -Dtest=AgendamentoServiceTest
.\mvnw.cmd test -Dtest=AgendamentoServiceTest#deveCobrarSomenteHorasCompletas
```

Credentials come from a git-ignored `.env` at the project root (`DB_USERNAME`, `DB_PASSWORD`; keys listed in `.env.example`), imported by `spring.config.import=optional:file:.env[.properties]`. Never read, print or copy `.env`, and never put credentials in a versioned file. `Could not resolve placeholder 'DB_USERNAME'` means the app was started outside the project root.

The dev database `fitzone` must exist (empty is fine); tables are created by Flyway.

## Database and migrations (Flyway)

Migrations live in `src/main/resources/db/migration/` as `V<n>__<descricao>.sql` (two underscores). They are the single source of truth for the schema — there is no `schema.sql`.

| Version | Content |
|---|---|
| V1 | `alunos` — `id INT AUTO_INCREMENT` PK, `cpf` UNIQUE + `CHECK` 11 digits, `email` UNIQUE, all columns NOT NULL |
| V2 | `ambientes` — `id INT` (manually assigned, not auto-increment), `tipo` discriminator, `CHECK` on tipo, `valor_hora > 0` and id range per tipo |
| V3 | `agendamentos` — FKs `aluno_id`/`ambiente_id` `ON DELETE RESTRICT`, `CHECK hora_fim > hora_inicio`, `CHECK valor_total > 0`, index `idx_agendamentos_conflito (ambiente_id, data_agendamento, hora_inicio)` |
| V4 | `servicos_adicionais` — `tipo` discriminator, FK `agendamento_id` `ON DELETE CASCADE`, `CHECK` that LOCKER has `quantidade > 0` and other types have `quantidade IS NULL` |

Rules:
- **Never edit a migration that has already been applied** — Flyway checksums them and refuses to start. Changes go in a new `V<n+1>__...sql` (e.g. `ALTER TABLE`; MySQL cannot alter a `CHECK`, so drop and re-add it).
- `spring.jpa.hibernate.ddl-auto=validate`: Hibernate never changes the schema; any entity/column change needs a matching migration or the app fails to start. `validate` checks tables, columns and types, not constraints or indexes.
- `spring.jpa.open-in-view=false`: all database access must happen inside services; DTOs are built from data already loaded.

## Architecture

Base package `com.fitzone`; `domain` holds business logic and knows nothing about HTTP, `api` holds everything REST-specific.

```
com.fitzone/
├── FitzoneApplication.java
├── domain/
│   ├── model/        JPA entities + enums TipoAmbiente, TipoServico
│   ├── repository/   Spring Data JPA interfaces
│   ├── service/      @Service per context (Aluno, Ambiente, Agendamento, Relatorio), constructor injection, @Transactional
│   └── exception/    domain exceptions — all unchecked (extend RuntimeException)
└── api/
    ├── controller/       @RestController per resource, under /api
    ├── dto/              request/response records + report records
    └── exceptionhandler/ ApiExceptionHandler (@RestControllerAdvice) + ErroResposta
```

REST routes: `/api/alunos` (CRUD + `GET /busca?cpf=`), `/api/ambientes` (CRUD; PUT edits only `observacoes`), `/api/agendamentos` (create, list, get, cancel + `POST /{id}/servicos`; no PUT by design — rescheduling is cancel + create), `/api/relatorios/{alunos/{id}, ambientes, faturamento, servicos}`. POST returns 201 with `Location`, DELETE returns 204.

### Inheritance mapping (SINGLE_TABLE)

`Ambiente` (abstract) and `ServicoAdicional` (abstract) use `InheritanceType.SINGLE_TABLE` with a `tipo` discriminator column. Subtypes override `getDescricao()`/`getTipo()` (`Ambiente`) or `getDescricao()`/`getValorTotal()` (`ServicoAdicional`) — real polymorphism despite the flattened table. `ServicoAdicional` was an interface in phase 1 and became an abstract `@Entity` because JPA cannot map interfaces; that is intentional. `TipoServico` enum values match the `@DiscriminatorValue`s.

### Ids

- `Aluno.id`, `Agendamento.id` (`Integer`) and `ServicoAdicional.id` (`Long`) are `IDENTITY`. CPF is a UNIQUE field, not the key — never put CPF in path variables (personal data).
- `Ambiente.id` is an `Integer` assigned by `AmbienteService` as `MAX(id) + 1` within the type's range from `TipoAmbiente` (101–110, 201–210, 301–310, 401–410). The range is duplicated in the V2 `CHECK` constraint on purpose — changing it requires changing both. Because the id is never null, `save()` performs a merge and would silently overwrite an existing row, so `AmbienteService` checks `existsById` before saving.

### DTOs are mandatory at the API boundary

`Agendamento` has EAGER fetches and a bidirectional relation with `ServicoAdicional` that would recurse infinitely in Jackson. Controllers always receive request records (validated with `@Valid`) and return `XResponse.from(entity)` — never a JPA entity. Validation of single fields lives in the DTOs; rules that compare fields live in services (or `ServicoRequest.toEntity()` for locker quantity). Every DB length/`CHECK` limit should have a matching Bean Validation annotation so it fails with 400 instead of a DB error.

### Business rules to preserve

- **Charged hours** — `Agendamento.getHorasCobradas()` is the single source of the duration rule: only complete hours count (2h40 → 2h), minimum 1 hour (40 min → 1h). Booking value, `PersonalTrainer` and reports must all use it; never recompute duration elsewhere (a duplicated version of this rule caused negative report values in phase 2).
- **Booking value** = `getHorasCobradas() × ambiente.valorHora` + sum of services. Prices: Musculação 100/h, Yoga 70/h, Crossfit 100/h, Piscina 80/h; Avaliação física 70, Nutricionista 80 (fixed); Personal trainer 50 × charged hours of its booking; Locker 5 per unit.
- **Time conflicts** are checked in the database: `AgendamentoRepository.buscarConflitos()` (JPQL, same ambiente and date, `horaInicio < :fim AND horaFim > :inicio`). Adjacent slots (8–9 and 9–10) do not conflict.
- **Deletes**: aluno/ambiente with bookings are protected by `ON DELETE RESTRICT`; services call `delete()` followed by `flush()` so the violation surfaces inside the method. Cancelling a booking removes its services (orphanRemoval + `ON DELETE CASCADE`).
- **Exception → HTTP status** (`ApiExceptionHandler`, grouped by category): not-found exceptions and unknown routes → 404; `AlunoJaCadastrado`/`AmbienteJaCadastrado`/`AmbienteIndisponivel`/`LimiteAmbienteExcedido` and `DataIntegrityViolationException` → 409; `HorarioInvalido`/`ServicoInvalido`, Bean Validation, unreadable JSON, type mismatch and missing params → 400; 405/415 for wrong verb/media type; anything else → 500 with a generic message (details only in the server log). When adding a domain exception, make it unchecked and add it to the matching group instead of creating a new response shape.

## Tests

Integration tests (`@SpringBootTest` + `@ActiveProfiles("test")`) exercise the services against a real MySQL database `fitzone_test`, configured in `src/test/resources/application-test.properties` (`createDatabaseIfNotExist=true`; Flyway creates the tables). Test classes are `@Transactional`, so each test is rolled back and the dev database `fitzone` is never touched. Consequences: `AUTO_INCREMENT` ids are not reset, so assertions must not depend on specific aluno/agendamento ids (ambiente ids are fine); and entities created in the test stay in the persistence context — tests that expect the database itself to reject an operation (e.g. FK violations) must call `entityManager.flush()` and `entityManager.clear()` first.

Manual test scenarios: Postman collection in `postman/` and the checklist in [Roteiro.md](Roteiro.md).

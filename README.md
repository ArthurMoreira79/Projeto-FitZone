# FitZone — Sistema de Gestão de Academia

Projeto desenvolvido a princípio para revisar conceitos da Programação Orientada a Objetos, simulando a gestão de uma academia/estúdio fitness: cadastro de alunos, ambientes reserváveis (sala de musculação, yoga, crossfit, piscina), agendamentos com serviços adicionais (avaliação física, nutricionista, personal trainer, locker) e relatórios de ocupação e faturamento.

O projeto evoluiu em fases — de um sistema de console em Java puro até uma **API REST com Spring Boot, MySQL e Flyway**. Este README documenta o estado atual e o histórico de cada fase.

---

## 1. Estado atual (Fase 4 concluída — API REST com Spring Boot)

- **API REST** com Spring Boot 4 (Java 21): CRUD de alunos e ambientes, agendamentos com serviços adicionais e 4 relatórios, em `/api/*`.
- **Arquitetura em camadas**: `domain` (modelo, repositórios, services, exceções) separado de `api` (controllers, DTOs, tratamento de erros) — as regras de negócio não sabem que existe HTTP.
- **Banco versionado com Flyway**: o schema é criado e evoluído por migrations (`src/main/resources/db/migration`); o Hibernate só valida (`ddl-auto=validate`).
- **Regras de negócio também no banco**: constraints `CHECK` (horário, valores, faixa de ids por tipo, tipos válidos, quantidade de lockers), `UNIQUE` (CPF, e-mail) e FKs com `ON DELETE RESTRICT` — última linha de defesa caso algum bug passe pelos services.
- **Modelagem OO preservada**: `Ambiente` e `ServicoAdicional` são classes abstratas com herança `SINGLE_TABLE` e polimorfismo real (cada subtipo calcula sua descrição e seu valor).
- **DTOs (records) na fronteira da API** com Bean Validation — as entidades JPA nunca são expostas no JSON.
- **Erros padronizados**: um `@RestControllerAdvice` converte cada exceção no status certo (400, 404, 405, 409, 415, 500), sempre no mesmo formato JSON, com a lista de campos inválidos quando é erro de validação.
- **Testes**: 18 testes de integração (JUnit 5 + `@SpringBootTest`) contra um banco MySQL próprio de testes (`fitzone_test`), mais uma coleção do Postman com os cenários de teste manual (`postman/` e [Roteiro.md](Roteiro.md)).
- **Credenciais fora do código**: lidas de um arquivo `.env` que não é versionado.

### Pendências conhecidas

Pontos identificados durante a Fase 4 e deixados conscientemente para depois:

- [ ] **Valores em `DOUBLE`**: `valor_hora` e `valor_total` deveriam ser `DECIMAL` (`BigDecimal` no Java) para evitar erro de arredondamento em dinheiro. Exige uma migration nova (V5) e trocar `double` nas entidades, no cálculo e nos relatórios.
- [ ] **Ids da faixa não são reaproveitados**: o próximo id de ambiente é `MAX + 1` do tipo. Se um ambiente do meio da faixa for excluído, o número não volta a ser usado — com 10 ids por tipo, a faixa pode esgotar antes da hora. Alternativa: procurar o primeiro id livre da faixa.
- [ ] **Excluir aluno/ambiente com agendamentos** devolve a mensagem genérica "Conflito de integridade" (vinda do banco). Uma checagem prévia no service (`existsByAluno_Id`) com exceção própria daria uma mensagem mais clara.
- [ ] **Relatórios agregam em memória** (`findAll()` + streams). Suficiente para o volume atual; com muitos dados, o ideal é consultas com `GROUP BY` no banco.

---

## 2. Roadmap de evolução

A ideia aqui é evoluir em fases, sem misturar muita coisa nova de uma vez. Cada fase assume que a anterior está estável.

### Fase 1 — Robustez e boas práticas (Java puro) ✔ (concluída)

✔ - Validações de entrada mais completas na camada de fronteira: formato de CPF, datas no passado, hora fim menor que hora início, quantidade de lockers negativa etc.
✔ - Javadoc nas classes principais (`AdministradorSistema`, `Agendamento`, `Ambiente`) — treina documentação de API, útil pra qualquer projeto futuro.
✔ - Testes automatizados com **JUnit 5** para a camada de controle, cobrindo as regras de negócio (sobreposição de horário, cálculo de valor total, exceções lançadas nos casos certos).
✔ - Centralizar tratamento de erro de entrada (cada menu repetia `try/catch` parecido) com um pequeno utilitário de leitura validada (`lerInteiro()`, `lerData()`) reaproveitável entre os menus.

### Fase 2 — Persistência com banco de dados relacional ✔ (concluída)

Trocar a serialização em `.dat` por um banco de verdade:

- ✔ Migrado de `ObjectOutputStream`/`ObjectInputStream` para **JPA/Hibernate + MySQL**.
- ✔ Tabelas modeladas em `sql/schema.sql`: `alunos`, `ambientes` (herança `SINGLE_TABLE`, coluna `tipo`), `agendamentos`, `servicos_adicionais` (herança `SINGLE_TABLE`, coluna `tipo`). *(Substituído por migrations Flyway na Fase 4.)*
- ✔ Repositórios reescritos com `EntityManager`, mantendo a mesma assinatura de métodos que o `AdministradorSistema` já usava (Repository Pattern na prática).
- ✔ `ServicoAdicional` convertida de interface para classe abstrata `@Entity` (decisão registrada: JPA não mapeia interfaces).
- ✔ ID de `Agendamento` passou de contador manual para `AUTO_INCREMENT` do MySQL.
- ✔ Otimizada a checagem de sobreposição de horário: `realizarAgendamento` não percorre mais todos os agendamentos em memória — agora usa `RepositorioAgendamentos.buscarConflitos(...)`, uma consulta JPQL que já filtra por ambiente/data/sobreposição direto no banco (normalmente retorna 0 ou 1 linha, em vez da tabela inteira).
- ✔ Roteiro de testes manuais (`Roteiro.md`) atualizado para a Fase 2.

**Bugs encontrados e corrigidos durante a validação da Fase 2** (achados testando o sistema de ponta a ponta contra o MySQL real, não eram específicos de JPA/Hibernate):
- `LeitorEntrada.lerHoraApos()` tinha a condição de validação invertida — aceitava hora de fim *anterior* à de início e rejeitava horários válidos. Corrigido.
- `AdministradorSistema.relatorioPorAmbiente()` calculava horas totais sem aplicar o piso mínimo de 1h (mesma regra já usada em `Agendamento.calculaValorTotal()`), o que gerava valores negativos no relatório quando um agendamento tinha fim antes do início (efeito colateral do bug acima). Corrigido para ficar consistente com a mesma regra.
- `hibernate.show_sql`/`hibernate.format_sql` desligados em `persistence.xml` — estavam poluindo o console com o SQL gerado a cada operação.

- **Decisão da época (revista na Fase 4)**: os testes do JUnit viraram testes de integração contra o MySQL real, no mesmo schema `fitzone` do desenvolvimento, e `mvn test` limpava as tabelas a cada execução. Um schema `fitzone_test` separado foi descartado porque exigiria manter dois schemas à mão. Com o Flyway criando as tabelas sozinho, esse custo sumiu — na Fase 4 os testes passaram a usar o `fitzone_test`.

### Fase 3 — Melhorias de interface (ainda em texto) ✔ (concluída)

- ✔ Cores ANSI no terminal (`ConsoleUtil`): vermelho para erro, verde para sucesso, amarelo para aviso; negrito + branco destacado para títulos, subtítulos e blocos de relatório.
- ✔ Validação de entrada com nova tentativa em vez de cancelar a operação inteira ao digitar algo errado, inclusive para CPF duplicado no cadastro de aluno, verificado assim que digitado.
- ✔ Pausa "Pressione ENTER para continuar" ao fim de cada operação, antes de voltar ao menu.
- ✔ Espaçamento padronizado entre mensagens comuns e mensagens de erro/sucesso/aviso (resolvido direto nos métodos do `ConsoleUtil`).
- ✔ Tabelas de listagem realinhadas e blocos do relatório de faturamento padronizados no estilo `===== TEXTO =====`.
- Não feito, opcional: **Lanterna**/**JLine** para menus mais ricos — ficou obsoleto, porque a interface de console foi removida na Fase 4.

### Fase 4 — API REST com Spring Boot ✔ (concluída)

A migração foi feita **à mão, passo a passo**, a partir do código da Fase 3. Uma primeira versão gerada com ajuda de IA ficou guardada na tag `fase4-teste`, como referência; a versão atual foi reescrita do zero para fins de aprendizado e aproveitou para revisar várias decisões.

**Estrutura e infraestrutura**
- ✔ Projeto gerado pelo Spring Initializr (Spring Boot 4, Java 21, Maven Wrapper) e código movido para o pacote base `com.fitzone`, organizado em `domain` e `api`.
- ✔ Credenciais em `.env` (fora do Git), importado pelo `application.properties`; o `.env.example` documenta as chaves.
- ✔ **Flyway**: o `schema.sql` aplicado à mão virou 4 migrations versionadas. Regra adotada: migration já aplicada nunca é editada — correções vão numa migration nova.
- ✔ Interface de console (`fronteira/`, `Main.java`) e persistência manual (`controle/`, `JPAUtil`, `persistence.xml`) removidas — o sistema agora é só API.

**Modelo e banco**
- ✔ `Aluno` ganhou **chave substituta** (`id INT AUTO_INCREMENT`); o CPF virou campo `UNIQUE`. Assim o CPF pode ser corrigido sem mexer em FKs e não aparece nas URLs da API (dado pessoal).
- ✔ `Ambiente.id` passou de `VARCHAR` para `INT`: o `MAX` do próximo id deixou de comparar texto (`"99" > "101"`) e as conversões `parseInt`/`valueOf` sumiram.
- ✔ Faixa de ids reduzida para **10 por tipo** (101–110, 201–210, 301–310, 401–410), garantida pelo enum `TipoAmbiente` e por uma constraint `CHECK` no banco.
- ✔ Constraints `CHECK` para horário (`fim > início`), valores positivos, tipos válidos e quantidade de lockers; índice para a consulta de conflito de horário.
- ✔ E-mail e telefone do aluno passaram a ser obrigatórios, e o e-mail, único.

**Regras de negócio revisadas**
- ✔ **Cobrança por hora completa**: só horas inteiras contam (2h40 → 2h), com mínimo de 1 hora. A regra ficou centralizada em `Agendamento.getHorasCobradas()`, usada no valor, no personal trainer e nos relatórios — a duplicação dessa regra foi a causa do bug de relatório da Fase 2.
- ✔ **Personal trainer cobrado por hora** do agendamento (antes era valor fixo, apesar do Javadoc dizer "por hora").
- ✔ Reagendar = cancelar + criar outro (não existe PUT de agendamento, de propósito: mudar o horário exigiria recalcular valor e checar conflito).

**Camadas**
- ✔ Repositórios viraram interfaces **Spring Data JPA**, com query methods (`existsByCpf`, `findByAluno_Id...`) e `@Query` para o conflito de horário e o `TYPE()` da herança.
- ✔ `AdministradorSistema` dividido em 4 `@Service` (`AlunoService`, `AmbienteService`, `AgendamentoService`, `RelatorioService`) com `@Transactional`.
- ✔ Exceções de domínio viraram **unchecked** (`RuntimeException`) — com exceção checked, o `@Transactional` não faz rollback. `FalhaPersistenciaException` foi removida (o Spring traduz erros de banco em `DataAccessException`).
- ✔ DTOs (records) com Bean Validation; controllers REST com 201 + `Location` no POST, 204 no DELETE e CRUD completo de alunos e ambientes.
- ✔ `ApiExceptionHandler` agrupando as exceções por categoria de status.

**Testes**
- ✔ 18 testes de integração dos services rodando no banco `fitzone_test` (criado automaticamente), com rollback ao fim de cada teste.
- ✔ Coleção do Postman versionada em `postman/`, com caminho feliz e casos de erro.

### Fase 5 — Interface gráfica ou Web

Duas direções possíveis a partir da Fase 4:

- **Opção A — JavaFX**: interface desktop, mantendo tudo em Java. Boa se você quiser focar em Java puro.
- **Opção B (recomendada, alinhada com seu objetivo de full stack)**: manter o Spring Boot como API REST e construir um front-end separado (React, ou até HTML/CSS/JS simples pra começar). Isso te dá prática real de full stack — exatamente o tipo de projeto que fica bem num portfólio.

### Fase 6 — Portfólio

- Documentar o projeto com prints de tela, diagrama de classes atualizado e instruções claras de setup (esse próprio README pode evoluir pra isso).
- Organizar o histórico do Git por fase (tags tipo `v1-poo`, `v2-jpa`, `v3-console`, `v4-spring-api`, `v5-web`), mostrando a evolução — isso conta muito mais do que só o código final.
- Um projeto que nasce simples (CRUD + regras de negócio) e evolui até virar API + front-end é um ótimo case pra mostrar a clientes ou recrutadores que você entende o ciclo completo, não só uma parte isolada.

---

## 3. Como rodar

**Pré-requisitos:** JDK 21 e MySQL **8.0.16 ou mais novo** rodando localmente (versões anteriores aceitam as constraints `CHECK` mas não as aplicam). Não é preciso instalar o Maven: o projeto traz o Maven Wrapper (`mvnw`).

**1. Criar o banco** (só o banco, vazio — as tabelas são criadas pelo Flyway):

```sql
CREATE DATABASE fitzone;
```

**2. Configurar as credenciais**: copie o `.env.example` para `.env` na raiz do projeto e preencha:

```properties
DB_USERNAME=root
DB_PASSWORD=sua_senha_do_mysql
```

O `.env` está no `.gitignore` e nunca deve ser commitado. Se aparecer `Could not resolve placeholder 'DB_USERNAME'`, a aplicação foi executada fora da raiz do projeto e não encontrou o arquivo.

**3. Subir a API** (na raiz do projeto):

```bash
.\mvnw.cmd spring-boot:run
```

(No Linux/macOS: `./mvnw spring-boot:run`.) Na primeira execução, o Flyway aplica as migrations e cria as tabelas. A API fica em `http://localhost:8080/api`.

**4. Rodar os testes:**

```bash
.\mvnw.cmd test
```

Os testes usam o banco **`fitzone_test`**, criado automaticamente na primeira execução (profile `test`, em `src/test/resources/application-test.properties`). Cada teste roda numa transação desfeita no final, então o banco de testes fica sempre vazio e **o banco `fitzone` de desenvolvimento nunca é tocado**.

**5. Testar manualmente:** a coleção do Postman está em `postman/` e o checklist dos cenários em [Roteiro.md](Roteiro.md).

---

## 4. API

| Recurso | Rotas |
|---|---|
| Alunos | `POST /api/alunos` · `GET /api/alunos` · `GET /api/alunos/{id}` · `GET /api/alunos/busca?cpf=` · `PUT /api/alunos/{id}` · `DELETE /api/alunos/{id}` |
| Ambientes | `POST /api/ambientes` · `GET /api/ambientes` · `GET /api/ambientes/{id}` · `PUT /api/ambientes/{id}` (só observações) · `DELETE /api/ambientes/{id}` |
| Agendamentos | `POST /api/agendamentos` · `GET /api/agendamentos` · `GET /api/agendamentos/{id}` · `POST /api/agendamentos/{id}/servicos` · `DELETE /api/agendamentos/{id}` |
| Relatórios | `GET /api/relatorios/alunos/{id}` · `GET /api/relatorios/ambientes` · `GET /api/relatorios/faturamento` · `GET /api/relatorios/servicos` |

Exemplos de corpo:

```jsonc
// POST /api/alunos
{ "cpf": "12345678901", "nome": "Ana Souza", "email": "ana@email.com", "telefone": "11987654321" }

// POST /api/ambientes   (tipo: MUSCULACAO | YOGA | CROSSFIT | PISCINA)
{ "tipo": "YOGA", "observacoes": "Sala climatizada" }

// POST /api/agendamentos
{ "alunoId": 1, "ambienteId": 101, "data": "2026-12-01", "horaInicio": "08:00", "horaFim": "10:40" }

// POST /api/agendamentos/{id}/servicos   (tipo: AVALIACAO_FISICA | NUTRICIONISTA | PERSONAL_TRAINER | LOCKER)
{ "tipo": "LOCKER", "quantidade": 2 }
```

Todo erro volta no mesmo formato:

```json
{
  "status": 400,
  "titulo": "Dados inválidos",
  "mensagem": "Um ou mais campos estão inválidos. Corrija e tente novamente.",
  "caminho": "/api/alunos",
  "dataHora": "2026-10-20T16:30:00",
  "campos": [ { "nome": "cpf", "mensagem": "O CPF deve conter exatamente 11 dígitos, sem pontos ou traço" } ]
}
```

### Regras de negócio e preços

| Item | Valor |
|---|---|
| Sala de Musculação (ids 101–110) | R$ 100,00/h |
| Sala de Yoga (ids 201–210) | R$ 70,00/h |
| Sala de Crossfit (ids 301–310) | R$ 100,00/h |
| Piscina (ids 401–410) | R$ 80,00/h |
| Avaliação física | R$ 70,00 (fixo) |
| Nutricionista | R$ 80,00 (fixo) |
| Personal trainer | R$ 50,00 por hora cobrada do agendamento |
| Locker | R$ 5,00 por unidade |

- **Horas cobradas**: só horas completas (2h40 → 2h); reservas menores que 1 hora pagam 1 hora cheia.
- **Conflito de horário**: dois agendamentos no mesmo ambiente e dia não podem se sobrepor; horários "encostados" (8h–9h e 9h–10h) são permitidos.
- **Exclusão**: aluno ou ambiente com agendamentos não pode ser excluído (409). Cancelar um agendamento remove os serviços dele.

---

## 5. Estrutura de pastas

```
Projeto-FitZone/
├── .env.example              # chaves de configuração (o .env real não é versionado)
├── postman/                  # coleção do Postman (testes manuais)
├── Roteiro.md                # checklist dos testes manuais
└── src/
    ├── main/
    │   ├── java/com/fitzone/
    │   │   ├── FitzoneApplication.java   # ponto de entrada Spring Boot
    │   │   ├── domain/
    │   │   │   ├── model/        # entidades JPA + enums TipoAmbiente e TipoServico
    │   │   │   ├── repository/   # interfaces Spring Data JPA
    │   │   │   ├── service/      # regras de negócio (@Service, @Transactional)
    │   │   │   └── exception/    # exceções de domínio (unchecked)
    │   │   └── api/
    │   │       ├── controller/       # @RestController por recurso
    │   │       ├── dto/              # records de request/response e dos relatórios
    │   │       └── exceptionhandler/ # ApiExceptionHandler + ErroResposta
    │   └── resources/
    │       ├── application.properties
    │       └── db/migration/     # migrations Flyway (V1..V4)
    └── test/
        ├── java/com/fitzone/domain/service/       # testes de integração dos services
        └── resources/application-test.properties  # profile de teste (banco fitzone_test)
```

# FitZone — Sistema de Gestão de Academia

Projeto desenvolvido a princípio para revisar conceitos da Programação Orientada a Objetos, simulando a gestão de uma academia/estúdio fitness: cadastro de alunos, ambientes reserváveis (sala de musculação, yoga, crossfit, piscina), agendamentos com serviços adicionais (avaliação física, nutricionista, personal trainer, locker) e relatórios de ocupação e faturamento.

Este README documenta o estado atual do projeto e um roadmap de evolução gradual, já pensando além da revisão.

---

## 1. Estado atual (Fase 4 concluída — arquitetura Spring Boot)

O que já está implementado e funcionando:

- **Modelagem OO completa**: classe abstrata `Ambiente` com 4 subclasses; `ServicoAdicional` (era interface na Fase 1, virou classe abstrata na Fase 2 para poder ser mapeada como entidade JPA) com 4 implementações — o polimorfismo continua real, cada subtipo sobrescreve `getDescricao()`/`getValorTotal()`.
- **Encapsulamento**: atributos privados, getters/setters, regras de cálculo dentro das próprias entidades (`Agendamento.calculaValorTotal()`).
- **Exceções customizadas** com mensagens contextuais (`AlunoJaCadastradoException`, `AmbienteIndisponivelException`, `FalhaPersistenciaException`, etc.), mapeadas para respostas HTTP pelo `ApiExceptionHandler` (Fase 4).
- **Persistência com JPA/Hibernate + MySQL** (Fase 2, migrada para Spring Data JPA na Fase 4): `Ambiente` e `ServicoAdicional` usam herança `SINGLE_TABLE` com coluna discriminadora. O ID de `Agendamento` é `AUTO_INCREMENT` do MySQL. DDL manual em `sql/schema.sql` (ver seção 3).
- **Camadas separadas**: `entidades`, `repository`, `service`, `controller`, `dto`, `web`, `excecoes` — cada uma com responsabilidade única.
- **Interface textual refinada (Fase 3, aposentada na Fase 4)**: a interface de console (cores ANSI, menus interativos) existiu até a Fase 3 e foi removida quando o projeto virou uma API REST — ver Fase 4 abaixo.
- **Arquitetura Spring Boot (Fase 4)**: a lógica de negócio de `AdministradorSistema` foi dividida em 4 `@Service` por contexto (`AlunoService`, `AmbienteService`, `AgendamentoService`, `RelatorioService`), os repositórios manuais viraram interfaces `Spring Data JPA` (`AlunoRepository`, `AmbienteRepository`, `AgendamentoRepository`, com `@Query` customizado para a checagem de conflito de horário e para o `TYPE()` sobre a herança SINGLE_TABLE), e uma camada REST (`@RestController`) foi adicionada sobre os 4 recursos (`/api/alunos`, `/api/ambientes`, `/api/agendamentos`, `/api/relatorios`). Um `@RestControllerAdvice` (`ApiExceptionHandler`) mapeia as exceções de negócio existentes para status HTTP (404/409/400/500), e DTOs (`dto/*`, records) evitam expor as entidades JPA diretamente — necessário porque `Agendamento` tem fetch EAGER e uma relação bidirecional com `ServicoAdicional` que causaria recursão infinita na serialização JSON. Bean Validation (`jakarta.validation`) nos DTOs de request substitui a validação que antes vivia na camada de fronteira, hoje removida.

### Pontas soltas a fechar antes de evoluir

Pequenos ajustes que valem a pena resolver **antes** de começar a mexer em arquitetura ou interface nova, pra não carregar dívida técnica adiante:

- [✔ ] Decidir sobre o atributo `disponivel` do `Ambiente`: usar de verdade (ex. marcar ambiente em manutenção) ou remover de vez — hoje ele não existe mais na classe, então é só confirmar que isso não aparece como pendência no diagrama entregue.
- [✔ ] Criar uma exceção própria para "ID de ambiente já cadastrado" em vez de reaproveitar `AmbienteIndisponivelException` (que semanticamente é sobre disponibilidade de horário, não duplicidade de cadastro).
- [✔ ] Documentar um roteiro de testes manuais — feito em `Roteiro.md`, com checklist completo (todas as exceções, todos os relatórios), atualizado na Fase 2 pra refletir a persistência em MySQL e na Fase 4 pra refletir os endpoints REST (comandos `curl` no lugar da navegação por menu).
- [x] Menus de console mais ricos (Lanterna/JLine) — ficou obsoleto: a interface de console inteira foi removida na Fase 4 em favor da API REST.

---

## 2. Roadmap de evolução

A ideia aqui é evoluir em fases, sem misturar muita coisa nova de uma vez. Cada fase assume que a anterior está estável.

### Fase 1 — Robustez e boas práticas (curto prazo, ainda em Java puro)

✔ - Validações de entrada mais completas na camada de fronteira: formato de CPF, datas no passado, hora fim menor que hora início, quantidade de lockers negativa etc.
✔ - Javadoc nas classes principais (`AdministradorSistema`, `Agendamento`, `Ambiente`) — treina documentação de API, útil pra qualquer projeto futuro.
✔ - Testes automatizados com **JUnit 5** para a camada de controle, cobrindo as regras de negócio (sobreposição de horário, cálculo de valor total, exceções lançadas nos casos certos).
✔ - Centralizar tratamento de erro de entrada (hoje cada menu repete `try/catch` parecido) — dá pra criar um pequeno utilitário de leitura validada (`lerInteiro()`, `lerData()`) reaproveitável entre os menus.

### Fase 2 — Persistência com banco de dados relacional ✔ (concluída)

Trocar a serialização em `.dat` por um banco de verdade:

- ✔ Migrado de `ObjectOutputStream`/`ObjectInputStream` para **JPA/Hibernate + MySQL**.
- ✔ Tabelas modeladas em `sql/schema.sql`: `alunos`, `ambientes` (herança `SINGLE_TABLE`, coluna `tipo`), `agendamentos`, `servicos_adicionais` (herança `SINGLE_TABLE`, coluna `tipo`).
- ✔ Repositórios reescritos com `EntityManager`, mantendo a mesma assinatura de métodos que o `AdministradorSistema` já usava (Repository Pattern na prática).
- ✔ `ServicoAdicional` convertida de interface para classe abstrata `@Entity` (decisão registrada: JPA não mapeia interfaces).
- ✔ ID de `Agendamento` passou de contador manual para `AUTO_INCREMENT` do MySQL.
- ✔ Otimizada a checagem de sobreposição de horário: `realizarAgendamento` não percorre mais todos os agendamentos em memória — agora usa `RepositorioAgendamentos.buscarConflitos(...)`, uma consulta JPQL que já filtra por ambiente/data/sobreposição direto no banco (normalmente retorna 0 ou 1 linha, em vez da tabela inteira). CPF/ID já são únicos naturalmente pela chave primária.
- ✔ Roteiro de testes manuais (`Roteiro.md`) atualizado para a Fase 2 (nota sobre comportamento do `AUTO_INCREMENT`, instrução de como simular `FalhaPersistenciaException` com MySQL em vez de `.dat`).

**Bugs encontrados e corrigidos durante a validação da Fase 2** (achados testando o sistema de ponta a ponta contra o MySQL real, não eram específicos de JPA/Hibernate):
- `LeitorEntrada.lerHoraApos()` tinha a condição de validação invertida — aceitava hora de fim *anterior* à de início e rejeitava horários válidos. Corrigido.
- `AdministradorSistema.relatorioPorAmbiente()` calculava horas totais sem aplicar o piso mínimo de 1h (mesma regra já usada em `Agendamento.calculaValorTotal()`), o que gerava valores negativos no relatório quando um agendamento tinha fim antes do início (efeito colateral do bug acima). Corrigido para ficar consistente com a mesma regra.
- `hibernate.show_sql`/`hibernate.format_sql` desligados em `persistence.xml` — estavam poluindo o console com o SQL gerado a cada operação; era útil só durante o desenvolvimento inicial da Fase 2.

- **Decisão consciente, não pendência**: os testes do JUnit viraram testes de integração contra o MySQL real (schema `fitzone`, mesmo usado em desenvolvimento) — rodam mais lento e dependem do banco estar no ar, e `mvn test` limpa as tabelas a cada execução. Avaliamos usar um schema `fitzone_test` separado (ou H2 em memória) só pro escopo de teste, mas decidimos que não vale a complexidade extra por enquanto. Fica registrado como possível evolução futura se o projeto crescer.

### Fase 3 — Melhorias de interface (ainda em texto) ✔ (concluída)

- ✔ Cores ANSI no terminal (`ConsoleUtil`): vermelho para erro, verde para sucesso, amarelo para aviso; negrito + branco destacado para títulos, subtítulos e blocos de relatório.
- ✔ Validação de entrada com nova tentativa em vez de cancelar a operação inteira ao digitar algo errado — já valia para formato (CPF, data, hora, inteiro) desde a Fase 1/2; na Fase 3 passou a valer também para CPF duplicado no cadastro de aluno, verificado assim que digitado, em loop, em vez de só no fim do cadastro.
- ✔ Pausa "Pressione ENTER para continuar" ao fim de cada operação, antes de voltar ao menu — dá tempo de ler a mensagem de conclusão sem apressar o fluxo.
- ✔ Espaçamento de uma linha padronizado entre qualquer mensagem comum do sistema e mensagens de erro/sucesso/aviso (resolvido direto nos métodos `erro()`/`sucesso()`/`aviso()` do `ConsoleUtil`, em vez de espalhar `println()` extras pelo código).
- ✔ Tabelas de listagem/busca realinhadas (Ambientes, Agendamentos, Relatório por Aluno) — colunas mais largas para acomodar nomes gerados automaticamente (ex. "Sala de Musculação 101") sem colar na coluna seguinte.
- ✔ Blocos internos do relatório de faturamento (por dia/ambiente/aluno) padronizados no mesmo estilo `===== TEXTO =====` usado em outros relatórios, com o mesmo destaque em negrito + branco.
- Não feito, opcional: bibliotecas como **Lanterna** ou **JLine** pra menus mais ricos (setas, seleção, cores nativas) — o README já tratava isso como "se quiser ir além do texto puro", fora do escopo obrigatório da fase. Fica como possível item futuro, não como pendência.

### Fase 4 — Arquitetura em camadas "de produção" (Spring Boot) ✔ (concluída)

- ✔ `AdministradorSistema` foi dividido em 4 classes `@Service` (`AlunoService`, `AmbienteService`, `AgendamentoService`, `RelatorioService`), uma por contexto de negócio.
- ✔ Repositórios viraram interfaces `Spring Data JPA` (`AlunoRepository`, `AmbienteRepository`, `AgendamentoRepository`), com `@Query` customizado para os dois casos que não são derived-query padrão (checagem de conflito de horário, e busca do maior ID por tipo via `TYPE()` sobre a herança SINGLE_TABLE).
- ✔ Entidades já tinham anotações JPA completas desde a Fase 2 — não precisaram de mudança estrutural nesta fase.
- ✔ Camada REST (`@RestController`) adicionada: `/api/alunos`, `/api/ambientes`, `/api/agendamentos`, `/api/relatorios`, com DTOs (records) para request/response e `@RestControllerAdvice` mapeando as exceções de negócio para status HTTP.
- ✔ Menu de console (`fronteira/`, `Main.java`) e persistência manual via `EntityManager` (`controle/`, `JPAUtil`, `persistence.xml`) foram removidos — o sistema agora é web-only, pronto para a Fase 5.
- ✔ Testes de integração adaptados: `AdministradorSistemaTest` foi dividido em `AlunoServiceTest`, `AmbienteServiceTest`, `AgendamentoServiceTest` (`src/test/java/service/`), testando os `@Service` via `@SpringBootTest` contra o MySQL real.

### Fase 5 — Interface gráfica ou Web

Duas direções possíveis a partir da Fase 4:

- **Opção A — JavaFX**: interface desktop, mantendo tudo em Java. Boa se você quiser focar em Java puro.
- **Opção B (recomendada, alinhada com seu objetivo de full stack)**: manter o Spring Boot como API REST e construir um front-end separado (React, ou até HTML/CSS/JS simples pra começar). Isso te dá prática real de full stack — exatamente o tipo de projeto que fica bem num portfólio.

### Fase 6 — Portfólio

- Documentar o projeto com prints de tela, diagrama de classes atualizado e instruções claras de setup (esse próprio README pode evoluir pra isso).
- Organizar o histórico do Git por fase (branches ou tags tipo `v1-poo`, `v2-jdbc`, `v3-spring-api`, `v4-web`), mostrando a evolução — isso conta muito mais do que só o código final.
- Um projeto que nasce simples (CRUD + regras de negócio) e evolui até virar API + front-end é um ótimo case pra mostrar a clientes ou recrutadores que você entende o ciclo completo, não só uma parte isolada.

---

## 3. Como rodar hoje (Spring Boot + Maven + MySQL)

Pré-requisitos: JDK 21, Maven e um MySQL rodando localmente com o banco `fitzone` já criado.

**Passo 1 — criar as tabelas** (uma vez só, ou sempre que `sql/schema.sql` mudar):

```bash
mysql -u SEU_USUARIO -p fitzone < sql/schema.sql
```

**Passo 2 — configurar credenciais**: `src/main/resources/application.properties` já aponta para `jdbc:mysql://localhost:3306/fitzone` com usuário `root`; a senha vem da variável de ambiente `DB_PASSWORD` (nunca fica em texto no repositório). Antes de rodar:

```powershell
$env:DB_PASSWORD = "sua_senha_do_mysql"
```

**Passo 3 — rodar**:

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Rotas disponíveis:

| Recurso | Rota |
|---|---|
| Alunos | `POST/GET /api/alunos`, `GET /api/alunos/{cpf}` |
| Ambientes | `POST/GET /api/ambientes`, `GET /api/ambientes/{id}` |
| Agendamentos | `POST/GET /api/agendamentos`, `GET/DELETE /api/agendamentos/{id}`, `POST /api/agendamentos/{id}/servicos` |
| Relatórios | `GET /api/relatorios/alunos/{cpf}`, `/ambientes`, `/faturamento`, `/servicos` |

Exemplo rápido:
```bash
curl -X POST http://localhost:8080/api/alunos -H "Content-Type: application/json" \
  -d '{"cpf":"12345678901","nome":"Teste","email":"t@ex.com","telefone":"11999999999"}'
```

**Rodar os testes** (testes de integração, batem no MySQL real e limpam as tabelas do schema `fitzone` a cada teste — precisa de `DB_PASSWORD` setado igual acima):

```bash
mvn test
```

## 4. Estrutura de pastas

```
fitzone/
├── app/          # FitzoneApplication — ponto de entrada Spring Boot
├── entidades/    # modelo de domínio (Aluno, Ambiente e subclasses, Agendamento, ServicoAdicional e subclasses), anotado com JPA
├── repository/   # interfaces Spring Data JPA (AlunoRepository, AmbienteRepository, AgendamentoRepository)
├── service/      # regras de negócio (AlunoService, AmbienteService, AgendamentoService, RelatorioService)
├── controller/   # camada REST (@RestController por recurso)
├── dto/          # records de request/response da API
├── web/          # @RestControllerAdvice (ApiExceptionHandler) e ApiErrorResponse
├── excecoes/     # exceções customizadas do domínio
└── sql/          # schema.sql — DDL manual das tabelas MySQL
```
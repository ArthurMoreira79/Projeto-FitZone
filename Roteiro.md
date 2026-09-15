# Roteiro de Testes Manuais — FitZone

Este roteiro cobre todas as funcionalidades do sistema e o disparo de cada exceção customizada, agora via **API REST** (Fase 4). Use-o como checklist antes da apresentação — e também como guia rápido caso o professor pergunte "o que acontece se eu fizer X".

Convenção: **[ ]** = ainda não testado · **[x]** = testado e OK.

> **Como rodar**: suba a aplicação com `mvn spring-boot:run` (lembrando de setar `$env:DB_PASSWORD` antes) e execute os comandos `curl` abaixo em outro terminal. Detalhes de setup completos no `README.md`.
>
> **Nota (Fase 2 — persistência MySQL):** os dados ficam em banco MySQL real (via JPA/Hibernate). Os IDs de agendamento (`AUTO_INCREMENT`) **não resetam sozinhos** ao apagar registros — é normal ver IDs altos mesmo numa tabela "vazia" se já rodou testes antes. Isso não é bug, é comportamento padrão de banco relacional.
>
> Rodar `mvn test` limpa as tabelas do schema `fitzone` — não rode os testes automatizados no meio de uma sessão deste roteiro manual, ou vai perder os dados que acabou de cadastrar.
>
> Os comandos abaixo usam sintaxe Bash (aspas simples). No **PowerShell**, escape as aspas duplas internas do JSON com `\"` (ex.: `-d '{\"cpf\":\"...\"}'`) ou use Postman/Insomnia.

---

## Módulo 1 — Alunos (`/api/alunos`)

| # | Cenário | Comando | Resultado esperado |
|---|---------|---------|---------------------|
| TC01 | Cadastrar aluno com sucesso | `curl -X POST http://localhost:8080/api/alunos -H "Content-Type: application/json" -d '{"cpf":"11111111111","nome":"Teste","email":"a@a.com","telefone":"11999999999"}'` | `201 Created` + JSON do aluno, com `dataCadastro` preenchido pelo servidor |
| TC02 | **Exceção:** CPF duplicado | Repetir TC01 com o mesmo CPF | `409 Conflict` — `AlunoJaCadastradoException` ("CPF já existe.") |
| TC03 | Buscar aluno existente | `curl http://localhost:8080/api/alunos/11111111111` | `200 OK` + dados do aluno |
| TC04 | **Exceção:** buscar aluno inexistente | `curl http://localhost:8080/api/alunos/99999999999` | `404 Not Found` — `AlunoNaoEncontradoException` |
| TC05 | Listar alunos | `curl http://localhost:8080/api/alunos` | `200 OK` — `[]` se vazio, ou array com todos os alunos cadastrados |
| TC05b | **Validação:** dado inválido | `curl -X POST .../api/alunos -d '{"cpf":"123","nome":"","email":"invalido","telefone":""}'` | `400 Bad Request` — mensagem de validação (CPF precisa de 11 dígitos, nome obrigatório, email inválido) |

---

## Módulo 2 — Ambientes (`/api/ambientes`)

| # | Cenário | Comando | Resultado esperado |
|---|---------|---------|---------------------|
| TC06 | Cadastrar um ambiente de cada tipo | `curl -X POST .../api/ambientes -d '{"tipo":"MUSCULACAO","observacoes":"..."}'`, repetir com `"YOGA"`, `"CROSSFIT"`, `"PISCINA"` | `201 Created` para cada um, com ID e nome gerados automaticamente (ex.: `"Sala de Musculação 101"`) |
| TC07 | **Exceção:** faixa de IDs do tipo esgotada | Cadastrar 20 ambientes `MUSCULACAO` seguidos (faixa 101–120) | Os 20 primeiros: `201`. O 21º: `409 Conflict` — `LimiteAmbienteExcedidoException` |
| TC08 | Buscar ambiente existente | `curl http://localhost:8080/api/ambientes/101` | `200 OK` + dados do ambiente |
| TC08b | **Exceção:** buscar ambiente inexistente | `curl http://localhost:8080/api/ambientes/999` | `404 Not Found` — `AmbienteNaoEncontradoException` |
| TC09 | Listar ambientes | `curl http://localhost:8080/api/ambientes` | `200 OK` — array com Tipo, ID, Nome e Valor/Hora de todos os cadastrados |

---

## Módulo 3 — Agendamentos e Serviços (`/api/agendamentos`)

| # | Cenário | Comando | Resultado esperado |
|---|---------|---------|---------------------|
| TC10 | Criar agendamento sem serviços | `curl -X POST .../api/agendamentos -d '{"alunoCpf":"11111111111","ambienteId":"101","dataAgendamento":"2026-12-01","horaInicio":"08:00","horaFim":"09:00"}'` | `201 Created`, `valorTotal` = 1h × valor/hora do ambiente, `servicos: []` |
| TC11 | Adicionar serviço a uma reserva existente | `curl -X POST .../api/agendamentos/{id}/servicos -d '{"tipo":"PERSONAL_TRAINER"}'` (tipos válidos: `PERSONAL_TRAINER`, `NUTRICIONISTA`, `AVALIACAO_FISICA`, `LOCKER` — este último exige `"quantidade"`) | `200 OK`, `valorTotal` atualizado somando o serviço, `servicos` com o novo item |
| TC12 | **Exceção:** aluno inexistente | Criar agendamento com `alunoCpf` não cadastrado | `404 Not Found` — `AlunoNaoEncontradoException` |
| TC13 | **Exceção:** ambiente inexistente | Criar agendamento com `ambienteId` não cadastrado | `404 Not Found` — `AmbienteNaoEncontradoException` |
| TC14 | **Exceção:** horário sobreposto | Criar novo agendamento para o mesmo `ambienteId`/data do TC10, horário que se sobrepõe (ex.: `08:30`–`09:30`) | `409 Conflict` — `AmbienteIndisponivelException` ("Ambiente já reservado das 08:00 às 09:00") |
| TC15 | **Exceção:** tipo de serviço inválido | `curl -X POST .../api/agendamentos/{id}/servicos -d '{"tipo":"INEXISTENTE"}'` | `400 Bad Request` — `ServicoInvalidoException` |
| TC16 | **Exceção:** ID de reserva inexistente (buscar/cancelar/add serviço) | `curl http://localhost:8080/api/agendamentos/9999` (ou `DELETE`, ou `POST .../servicos`) | `404 Not Found` — `AgendamentoNaoEncontradoException` |
| TC17 | Cancelar agendamento | `curl -X DELETE http://localhost:8080/api/agendamentos/{id}` | `204 No Content`; buscar o mesmo ID depois retorna `404` |
| TC18 | Listar agendamentos | `curl http://localhost:8080/api/agendamentos` | `200 OK` — array com Aluno, Ambiente, Data, Horário, Total e Serviços de cada agendamento ativo |
| TC18b | **Validação:** dado inválido | `curl -X POST .../api/agendamentos -d '{"alunoCpf":"","ambienteId":"","dataAgendamento":null,"horaInicio":null,"horaFim":null}'` | `400 Bad Request` — mensagem de validação (campos obrigatórios) |

---

## Módulo 4 — Relatórios (`/api/relatorios`, somente leitura)

| # | Cenário | Comando | Resultado esperado |
|---|---------|---------|---------------------|
| TC19 | Relatório por aluno — com dados | `curl http://localhost:8080/api/relatorios/alunos/11111111111` | `200 OK` — array com as reservas daquele aluno |
| TC19b | Relatório por aluno — sem dados | `curl http://localhost:8080/api/relatorios/alunos/22222222222` (sem reservas) | `200 OK` — `[]` |
| TC20 | Relatório de utilização de ambientes | `curl http://localhost:8080/api/relatorios/ambientes` | `200 OK` — array com ambiente, quantidade de agendamentos e total de horas usadas |
| TC21 | Relatório de faturamento | `curl http://localhost:8080/api/relatorios/faturamento` | `200 OK` — objeto com `porDia`, `porAmbiente`, `porAluno` e `total`, batendo com os agendamentos criados |
| TC22 | Relatório de serviços adicionais | `curl http://localhost:8080/api/relatorios/servicos` | `200 OK` — array com tipo de serviço, quantidade vendida e valor total arrecadado |

---

## Resumo — onde cada exceção é disparada

| Exceção | Status HTTP | Onde testar | Caso(s) |
|---|---|---|---|
| `AlunoJaCadastradoException` | 409 | `POST /api/alunos`, com CPF repetido | TC02 |
| `AlunoNaoEncontradoException` | 404 | `GET /api/alunos/{cpf}` ou `POST /api/agendamentos`, com CPF inexistente | TC04, TC12 |
| `AmbienteJaCadastradoException` | 409 | (checagem defensiva interna — não deveria disparar em uso normal, já que o ID é sempre gerado como livre) | — |
| `AmbienteNaoEncontradoException` | 404 | `GET /api/ambientes/{id}` ou `POST /api/agendamentos`, com ID inexistente | TC08b, TC13 |
| `LimiteAmbienteExcedidoException` | 409 | `POST /api/ambientes`, ao esgotar a faixa de 20 IDs de um tipo | TC07 |
| `AmbienteIndisponivelException` | 409 | `POST /api/agendamentos`, com horário sobreposto no mesmo ambiente/data | TC14 |
| `AgendamentoNaoEncontradoException` | 404 | `GET`/`DELETE /api/agendamentos/{id}` ou `POST .../servicos`, com ID inexistente | TC16 |
| `ServicoInvalidoException` | 400 | `POST /api/agendamentos/{id}/servicos`, com tipo de serviço inválido/nulo | TC15 |
| Bean Validation (`MethodArgumentNotValidException`) | 400 | Qualquer `POST` com campo obrigatório vazio/inválido | TC05b, TC18b |

`FalhaPersistenciaException` não está neste roteiro com um TC numerado por exigir simular indisponibilidade do banco, mas fica documentado como testar: pare o serviço do MySQL local (ou troque `DB_PASSWORD` para um valor incorreto) e tente subir a aplicação com `mvn spring-boot:run` — deve falhar já na inicialização com um erro claro do Spring/Hibernate, sem o servidor chegar a aceitar requisições.

---

## Sugestão de ordem de execução

Para não perder dados de um teste pro outro, rode nesta ordem numa mesma sessão: **Alunos (TC01–TC05b) → Ambientes (TC06–TC09) → Agendamentos (TC10–TC18b) → Relatórios (TC19–TC22)**. Assim os relatórios já têm dados reais pra mostrar, e você testa as exceções de "não encontrado" antes de cadastrar o que falta, e as de "duplicado"/"conflito" depois.

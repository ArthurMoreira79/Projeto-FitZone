# Roteiro de Testes Manuais — FitZone (API REST)

Este roteiro cobre todos os endpoints da API e o disparo de cada exceção e status de erro. Use-o como checklist antes de uma apresentação — e também como guia rápido caso alguém pergunte "o que acontece se eu fizer X".

As requisições estão prontas na coleção do Postman em `postman/` (pastas Alunos, Ambientes, Agendamentos, Relatórios e Erros). Nas tabelas, as rotas omitem o prefixo `http://localhost:8080/api`.

Convenção: **[ ]** = ainda não testado · **[x]** = testado e OK.

> **Antes de começar**
> - Os ids esperados abaixo assumem um banco **vazio**. Para recomeçar do zero: `DROP DATABASE fitzone; CREATE DATABASE fitzone;` no MySQL e suba a aplicação de novo (o Flyway recria as tabelas).
> - Ids `AUTO_INCREMENT` (alunos, agendamentos, serviços) **não voltam** ao apagar registros: depois de excluir algo, o próximo id continua a sequência. Não é bug, é o comportamento padrão do MySQL.
> - Rode os casos **na ordem**: os de agendamento dependem dos alunos e ambientes criados antes, e os relatórios conferem os valores acumulados.
> - Os testes automatizados (`mvnw test`) usam o banco `fitzone_test` e **não apagam** os dados deste roteiro.
> - Datas de agendamento precisam ser futuras: se `2026-12-01` já tiver passado, use qualquer data futura.

---

## Módulo 1 — Alunos

| # | Cenário | Requisição / Corpo | Resultado esperado |
|---|---------|--------------------|--------------------|
| TC01 | Cadastrar aluno | `POST /alunos` · `{"cpf":"12345678901","nome":"Ana Souza","email":"ana@email.com","telefone":"11987654321"}` | **201**, `id: 1`, `dataCadastro` = hoje, header `Location: .../api/alunos/1` |
| TC02 | Cadastrar segundo aluno | `POST /alunos` · `{"cpf":"98765432100","nome":"Bruno Lima","email":"bruno@email.com","telefone":"11912345678"}` | **201**, `id: 2` |
| TC03 | Listar alunos | `GET /alunos` | **200**, lista com os 2 alunos |
| TC04 | Buscar por id | `GET /alunos/1` | **200**, dados da Ana |
| TC05 | Buscar por CPF | `GET /alunos/busca?cpf=12345678901` | **200**, dados da Ana |
| TC06 | Atualizar aluno | `PUT /alunos/1` · JSON do TC01 com `"nome":"Ana Souza Lima"` | **200**, nome novo e `dataCadastro` **igual** à do TC01 |
| TC07 | **Erro:** aluno inexistente | `GET /alunos/99` | **404** "Recurso não encontrado" |
| TC08 | **Erro:** CPF duplicado | `POST /alunos` · JSON do TC01 | **409** "CPF 12345678901 já cadastrado." |
| TC09 | **Erro:** e-mail duplicado | `POST /alunos` · JSON do TC02 com outro CPF e `"email":"ana@email.com"` | **409** "E-mail ana@email.com já cadastrado." |
| TC10 | **Erro:** CPF de outro aluno na atualização | `PUT /alunos/2` · JSON do TC02 com `"cpf":"12345678901"` | **409** "já pertence a outro aluno" |
| TC11 | **Erro:** campos inválidos | `POST /alunos` · `{"cpf":"123","nome":"","email":"x","telefone":"1"}` | **400** "Dados inválidos", com 4 itens em `campos` |
| TC12 | **Erro:** id com tipo errado | `GET /alunos/abc` | **400** "Parâmetro inválido" |
| TC13 | **Erro:** parâmetro ausente | `GET /alunos/busca` (sem `?cpf=`) | **400** "Informe o parâmetro 'cpf'." |
| TC14 | **Erro:** verbo não suportado | `DELETE /alunos` (sem id) | **405** "Método não permitido" |

---

## Módulo 2 — Ambientes

| # | Cenário | Requisição / Corpo | Resultado esperado |
|---|---------|--------------------|--------------------|
| TC15 | Cadastrar ambiente | `POST /ambientes` · `{"tipo":"MUSCULACAO","observacoes":"Térreo"}` | **201**, `id: 101`, `nome: "Sala de Musculação 101"`, `valorHora: 100.0` |
| TC16 | Cadastrar ambiente de outro tipo | `POST /ambientes` · `{"tipo":"YOGA"}` | **201**, `id: 201`, `valorHora: 70.0` |
| TC17 | Listar ambientes | `GET /ambientes` | **200**, lista com 101 e 201 |
| TC18 | Atualizar observações | `PUT /ambientes/201` · `{"observacoes":"Sala climatizada"}` | **200**, observações novas; id, nome e valor inalterados |
| TC19 | **Erro:** tipo inexistente | `POST /ambientes` · `{"tipo":"BOXE"}` | **400** "Corpo da requisição inválido" |
| TC20 | **Erro:** JSON malformado | `POST /ambientes` · `{"tipo":` | **400** "Corpo da requisição inválido" |
| TC21 | **Erro:** faixa do tipo esgotada | Repetir `POST /ambientes` · `{"tipo":"YOGA"}` 10 vezes | As 9 primeiras: **201** (ids 202 a 210) · a 10ª: **409** "faixa 201-210 esgotada" |
| TC22 | **Erro:** formato diferente de JSON | `POST /ambientes` com Body em **Text** (não JSON) | **415** "Formato não suportado" |

---

## Módulo 3 — Agendamentos e Serviços

| # | Cenário | Requisição / Corpo | Resultado esperado |
|---|---------|--------------------|--------------------|
| TC23 | Criar agendamento | `POST /agendamentos` · `{"alunoId":1,"ambienteId":101,"data":"2026-12-01","horaInicio":"08:00","horaFim":"10:40"}` | **201**, `id: 1`, `horasCobradas: 2` (2h40 → 2h), `valorTotal: 200.0` |
| TC24 | Adicionar personal trainer | `POST /agendamentos/1/servicos` · `{"tipo":"PERSONAL_TRAINER"}` | **200**, `valorTotal: 300.0` (2h × R$ 50 de personal) |
| TC25 | Adicionar locker | `POST /agendamentos/1/servicos` · `{"tipo":"LOCKER","quantidade":2}` | **200**, `valorTotal: 310.0` |
| TC26 | Buscar agendamento | `GET /agendamentos/1` | **200**, com os 2 serviços na lista `servicos` |
| TC27 | Listar agendamentos | `GET /agendamentos` | **200** |
| TC28 | **Erro:** locker sem quantidade | `POST /agendamentos/1/servicos` · `{"tipo":"LOCKER"}` | **400** "Informe a quantidade de lockers." |
| TC29 | **Erro:** horário sobreposto | `POST /agendamentos` · `{"alunoId":2,"ambienteId":101,"data":"2026-12-01","horaInicio":"09:00","horaFim":"11:00"}` | **409** "Ambiente já reservado das 08:00 às 10:40." |
| TC30 | Horário encostado é permitido | Mesmo do TC29 com `"horaInicio":"10:40","horaFim":"12:00"` | **201**, `id: 2` |
| TC31 | **Erro:** fim antes do início | `POST /agendamentos` · `{"alunoId":2,"ambienteId":201,"data":"2026-12-01","horaInicio":"10:00","horaFim":"09:00"}` | **400** "A hora de fim (...) deve ser posterior à hora de início (...)" |
| TC32 | **Erro:** data no passado | `POST /agendamentos` · `{"alunoId":2,"ambienteId":201,"data":"2020-01-01","horaInicio":"08:00","horaFim":"09:00"}` | **400**, item `data` em `campos` |
| TC33 | **Erro:** ambiente inexistente | `POST /agendamentos` · `{"alunoId":2,"ambienteId":999,"data":"2026-12-02","horaInicio":"08:00","horaFim":"09:00"}` | **404** "Ambiente de id 999 não encontrado." |
| TC34 | **Erro:** excluir aluno com agendamentos | `DELETE /alunos/1` | **409** "Conflito de integridade" |
| TC35 | **Erro:** excluir ambiente com agendamentos | `DELETE /ambientes/101` | **409** "Conflito de integridade" |
| TC36 | Cancelar agendamento | `DELETE /agendamentos/2` | **204**, sem corpo |
| TC37 | **Erro:** agendamento cancelado | `GET /agendamentos/2` | **404** |

---

## Módulo 4 — Relatórios

Valores esperados considerando o estado após o TC37 (só o agendamento #1 existe, com total de R$ 310,00).

| # | Cenário | Requisição | Resultado esperado |
|---|---------|------------|--------------------|
| TC38 | Relatório por aluno | `GET /relatorios/alunos/1` | **200**, `quantidadeAgendamentos: 1`, `horasCobradas: 2`, `valorTotal: 310.0` |
| TC39 | Ocupação por ambiente | `GET /relatorios/ambientes` | **200**, 101 com 1 agendamento e 2h; 201 a 210 com 0 |
| TC40 | Faturamento | `GET /relatorios/faturamento` | **200**, `total: 310.0`, separado por dia, ambiente e aluno |
| TC41 | Serviços adicionais | `GET /relatorios/servicos` | **200**, `LockerAcademia` (1, R$ 10,00) e `PersonalTrainer` (1, R$ 100,00) |
| TC42 | **Erro:** endpoint inexistente | `GET /alunoss` | **404** "Endpoint não encontrado" |

---

## Resumo — onde cada erro é disparado

| Exceção / situação | Status | Caso(s) |
|---|---|---|
| `AlunoNaoEncontradoException` | 404 | TC07 |
| `AmbienteNaoEncontradoException` | 404 | TC33 |
| `AgendamentoNaoEncontradoException` | 404 | TC37 |
| Rota inexistente (`NoResourceFoundException`) | 404 | TC42 |
| `AlunoJaCadastradoException` | 409 | TC08, TC09, TC10 |
| `LimiteAmbienteExcedidoException` | 409 | TC21 |
| `AmbienteIndisponivelException` | 409 | TC29 |
| FK em uso (`DataIntegrityViolationException`) | 409 | TC34, TC35 |
| Bean Validation (`@Valid`) | 400 | TC11, TC32 |
| JSON malformado ou enum inválido | 400 | TC19, TC20 |
| `HorarioInvalidoException` | 400 | TC31 |
| `ServicoInvalidoException` | 400 | TC28 |
| Parâmetro com tipo errado / ausente | 400 | TC12, TC13 |
| Verbo não suportado | 405 | TC14 |
| Corpo que não é JSON | 415 | TC22 |

`AmbienteJaCadastradoException` não tem caso neste roteiro: ela protege o cadastro caso o id calculado para um novo ambiente já exista (o `save()` com id preenchido sobrescreveria o registro), o que não acontece pela API em uso normal.

O status **500** ("Erro interno") só aparece para erros não previstos. Para vê-lo, pare o MySQL com a aplicação no ar e faça qualquer requisição: a resposta é genérica e o detalhe técnico fica apenas no log do servidor.

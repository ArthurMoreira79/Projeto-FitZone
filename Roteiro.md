# Roteiro de Testes Manuais — FitZone (API REST)

Este roteiro cobre todos os endpoints da API, o disparo de cada exceção e status de erro e, no Módulo 6, as telas do front-end. Use-o como checklist antes de uma apresentação — e também como guia rápido caso alguém pergunte "o que acontece se eu fizer X".

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
| TC15 | Cadastrar ambiente | `POST /ambientes` · `{"tipo":"MUSCULACAO","observacoes":"Térreo"}` | **201**, `id: 101`, `nome: "Sala de Musculação 101"`, `valorHora: 100.00` |
| TC16 | Cadastrar ambiente de outro tipo | `POST /ambientes` · `{"tipo":"YOGA"}` | **201**, `id: 201`, `valorHora: 70.00` |
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
| TC23 | Criar agendamento | `POST /agendamentos` · `{"alunoId":1,"ambienteId":101,"data":"2026-12-01","horaInicio":"08:00","horaFim":"10:40"}` | **201**, `id: 1`, `horasCobradas: 2` (2h40 → 2h), `valorTotal: 200.00` |
| TC24 | Adicionar personal trainer | `POST /agendamentos/1/servicos` · `{"tipo":"PERSONAL_TRAINER"}` | **200**, `valorTotal: 300.00` (2h × R$ 50 de personal) |
| TC25 | Adicionar locker | `POST /agendamentos/1/servicos` · `{"tipo":"LOCKER","quantidade":2}` | **200**, `valorTotal: 310.00` |
| TC26 | Buscar agendamento | `GET /agendamentos/1` | **200**, com os 2 serviços na lista `servicos` |
| TC27 | Listar agendamentos | `GET /agendamentos` | **200** |
| TC28 | **Erro:** locker sem quantidade | `POST /agendamentos/1/servicos` · `{"tipo":"LOCKER"}` | **400** "Informe a quantidade de lockers." |
| TC29 | **Erro:** horário sobreposto | `POST /agendamentos` · `{"alunoId":2,"ambienteId":101,"data":"2026-12-01","horaInicio":"09:00","horaFim":"11:00"}` | **409** "Ambiente já reservado das 08:00 às 10:40." |
| TC30 | Horário encostado é permitido | Mesmo do TC29 com `"horaInicio":"10:40","horaFim":"12:00"` | **201**, `id: 2` |
| TC31 | **Erro:** fim antes do início | `POST /agendamentos` · `{"alunoId":2,"ambienteId":201,"data":"2026-12-01","horaInicio":"10:00","horaFim":"09:00"}` | **400** "A hora de fim (...) deve ser posterior à hora de início (...)" |
| TC32 | **Erro:** data no passado | `POST /agendamentos` · `{"alunoId":2,"ambienteId":201,"data":"2020-01-01","horaInicio":"08:00","horaFim":"09:00"}` | **400**, item `data` em `campos` |
| TC33 | **Erro:** ambiente inexistente | `POST /agendamentos` · `{"alunoId":2,"ambienteId":999,"data":"2026-12-02","horaInicio":"08:00","horaFim":"09:00"}` | **404** "Ambiente de id 999 não encontrado." |
| TC34 | **Erro:** excluir aluno com agendamentos | `DELETE /alunos/1` | **409** "O aluno Ana Souza Lima tem 1 agendamento e não pode ser excluído. Cancele os agendamentos antes." |
| TC35 | **Erro:** excluir ambiente com agendamentos | `DELETE /ambientes/101` | **409** "O ambiente Sala de Musculação 101 tem 2 agendamentos e não pode ser excluído. ..." |
| TC36 | Cancelar agendamento | `DELETE /agendamentos/2` | **204**, sem corpo |
| TC37 | **Erro:** agendamento cancelado | `GET /agendamentos/2` | **404** |

---

## Módulo 4 — Relatórios

Valores esperados considerando o estado após o TC37 (só o agendamento #1 existe, com total de R$ 310,00).

| # | Cenário | Requisição | Resultado esperado |
|---|---------|------------|--------------------|
| TC38 | Relatório por aluno | `GET /relatorios/alunos/1` | **200**, `quantidadeAgendamentos: 1`, `horasCobradas: 2`, `valorTotal: 310.00` |
| TC39 | Ocupação por ambiente | `GET /relatorios/ambientes` | **200**, 101 com 1 agendamento e 2h; 201 a 210 com 0 |
| TC40 | Faturamento | `GET /relatorios/faturamento` | **200**, `total: 310.00`, separado por dia, ambiente e aluno |
| TC41 | Serviços adicionais | `GET /relatorios/servicos` | **200**, `Personal Trainer` (1, R$ 100,00) e `Locker` (1, R$ 10,00), na ordem do enum |
| TC42 | **Erro:** endpoint inexistente | `GET /alunoss` | **404** "Endpoint não encontrado" |

No TC39, cada ambiente também traz `codigo` (`"MUSCULACAO"`) e `valorTotal` (101 com R$ 310,00).

---

## Módulo 5 — Endpoints da Fase 5 (usados pelo front-end)

| # | Cenário | Requisição | Resultado esperado |
|---|---------|------------|--------------------|
| TC43 | Tipos de ambiente | `GET /ambientes/tipos` | **200**, 4 tipos com `codigo`, `nome`, `valorHora`, `inicioFaixa`, `fimFaixa` |
| TC44 | Tipos de serviço | `GET /servicos/tipos` | **200**, 4 serviços com `valor` e `cobranca` (`FIXO`, `FIXO`, `POR_HORA`, `POR_UNIDADE`) |
| TC45 | Agendamentos de um dia e ambiente | `GET /agendamentos?data=2026-12-01&ambienteId=101` | **200**, só o agendamento #1, com `ambienteCodigo: "MUSCULACAO"` |
| TC46 | Agendamentos de um aluno | `GET /agendamentos?alunoId=1` | **200**, em ordem de data e hora |
| TC47 | **Erro:** data em outro formato | `GET /agendamentos?data=01/12/2026` | **400** "Parâmetro inválido" |
| TC48 | Orçamento | `GET /agendamentos/orcamento?ambienteId=101&horaInicio=08:00&horaFim=10:40` | **200**, `horasCobradas: 2`, `valorTotal: 200.00`; nada é gravado (`GET /agendamentos` continua igual) |
| TC49 | Orçamento abaixo de 1 hora | Mesmo do TC48 com `horaFim=08:40` | **200**, `horasCobradas: 1` |
| TC50 | **Erro:** orçamento com fim antes do início | Mesmo do TC48 com `horaFim=07:00` | **400** "A hora de fim (...) deve ser posterior à hora de início (...)" |
| TC51 | Locker com 1 unidade | `POST /agendamentos/1/servicos` · `{"tipo":"LOCKER","quantidade":1}` | **200**, descrição "Locker (1 unidade)", no singular |

---

## Módulo 6 — Fase 6 (acabamentos)

Continua do estado após o TC51: o agendamento #1 tem personal trainer, locker com 2 unidades e locker com 1 unidade (total R$ 315,00).

| # | Cenário | Requisição | Resultado esperado |
|---|---------|------------|--------------------|
| TC52 | Valores em decimal | `GET /agendamentos/1` | **200**, `valorTotal: 315.00`: dinheiro sai com duas casas (`DECIMAL` no banco, `BigDecimal` no Java) |
| TC53 | Remover serviço | Pegue em `servicos` (TC52) o `id` do "Locker (1 unidade)" · `DELETE /agendamentos/1/servicos/{id}` | **200**, `valorTotal: 310.00` e o locker de 1 unidade some da lista |
| TC54 | **Erro:** serviço inexistente no agendamento | `DELETE /agendamentos/1/servicos/99999` | **404** "Serviço de id 99999 não encontrado no agendamento 1." |
| TC55 | Id de ambiente volta a ser usado | `DELETE /ambientes/205` (criado no TC21, sem agendamentos) e depois `POST /ambientes` · `{"tipo":"YOGA"}` | **204** e depois **201** com `id: 205` (o primeiro livre da faixa, não 211) |

---

## Módulo 7 — Front-end

Com a API e o `npm run dev` rodando, em `http://localhost:5173`.

| # | Cenário | Resultado esperado |
|---|---------|--------------------|
| TF01 | Abrir o sistema | Painel com os números de hoje, a lista do dia e os próximos agendamentos; sem erros no Console (F12) |
| TF02 | API desligada | Cada tela mostra "Não foi possível conectar ao servidor..." com **Tentar de novo**; ao religar e clicar, os dados aparecem |
| TF03 | Endereço inexistente (`/qualquercoisa`) | "Página não encontrada", com o menu no topo |
| TF04 | Ambientes: cadastrar | Tipo lotado aparece "(sem vagas)" e desativado; o novo ambiente entra no bloco do tipo, com a anilha da cor certa |
| TF05 | Ambientes: excluir com agendamentos | Aviso vermelho com a mensagem da API: "O ambiente ... tem N agendamentos e não pode ser excluído. ..." |
| TF06 | Alunos: buscar | "joao" encontra "João"; um pedaço do CPF com pontos também encontra |
| TF07 | Alunos: CPF duplicado | Mensagem do 409 no topo do formulário; o que foi digitado continua lá |
| TF08 | Novo agendamento | Mostra os horários já reservados do ambiente no dia, marca os conflitos e desativa o botão; a prévia mostra as horas cobradas (2h40 → 2h) |
| TF09 | Detalhe: adicionar serviço | A quantidade só aparece para o locker; o total do detalhe e da lista se atualizam |
| TF10 | Detalhe: cancelar | Confirmação por cima do detalhe; Esc fecha só a confirmação; ao confirmar, o agendamento some da lista |
| TF11 | Filtros na URL | Agendamentos filtrados por data e ambiente continuam filtrados depois do F5 |
| TF12 | Relatórios | Totais batem com os agendamentos; dica ao passar o mouse ou focar com Tab numa coluna; "Ver como tabela" |
| TF14 | Detalhe: remover serviço | Cada serviço tem um botão **Remover**; ao clicar, o serviço sai da composição, o total do detalhe e da lista diminuem e aparece "Serviço removido: ..." dentro do modal |
| TF13 | Celular (`Ctrl+Shift+M` no DevTools) | Nenhuma tela rola para o lado; tabelas rolam dentro da própria caixa |

---

## Resumo — onde cada erro é disparado

| Exceção / situação | Status | Caso(s) |
|---|---|---|
| `AlunoNaoEncontradoException` | 404 | TC07 |
| `AmbienteNaoEncontradoException` | 404 | TC33 |
| `AgendamentoNaoEncontradoException` | 404 | TC37 |
| `ServicoNaoEncontradoException` | 404 | TC54 |
| Rota inexistente (`NoResourceFoundException`) | 404 | TC42 |
| `AlunoJaCadastradoException` | 409 | TC08, TC09, TC10 |
| `LimiteAmbienteExcedidoException` | 409 | TC21 |
| `AmbienteIndisponivelException` | 409 | TC29 |
| `RegistroEmUsoException` (aluno/ambiente com agendamentos) | 409 | TC34, TC35 |
| Banco recusou (`DataIntegrityViolationException`) | 409 | — (última linha de defesa: os services barram antes) |
| Bean Validation (`@Valid`) | 400 | TC11, TC32 |
| JSON malformado ou enum inválido | 400 | TC19, TC20 |
| `HorarioInvalidoException` | 400 | TC31 |
| `ServicoInvalidoException` | 400 | TC28 |
| Parâmetro com tipo errado / ausente | 400 | TC12, TC13 |
| Verbo não suportado | 405 | TC14 |
| Corpo que não é JSON | 415 | TC22 |

`AmbienteJaCadastradoException` não tem caso neste roteiro: ela protege o cadastro caso o id calculado para um novo ambiente já exista (o `save()` com id preenchido sobrescreveria o registro), o que não acontece pela API em uso normal.

`DataIntegrityViolationException` também não tem caso: desde a Fase 6 o service conta os agendamentos antes de excluir e responde com `RegistroEmUsoException`; o `ON DELETE RESTRICT` do banco só entraria em ação se essa checagem falhasse.

O status **500** ("Erro interno") só aparece para erros não previstos. Para vê-lo, pare o MySQL com a aplicação no ar e faça qualquer requisição: a resposta é genérica e o detalhe técnico fica apenas no log do servidor.

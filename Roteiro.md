# Roteiro de Testes Manuais — FitZone

Este roteiro cobre todas as funcionalidades do sistema e o disparo de cada exceção customizada (exceto `FalhaPersistenciaException`, que exige simular falha de disco/arquivo corrompido e não é coberta aqui). Use-o como checklist antes da apresentação — e também como guia rápido caso o professor pergunte "o que acontece se eu fizer X".

Convenção: **[ ]** = ainda não testado · **[x]** = testado e OK.

---

## Módulo 1 — Alunos

| # | Cenário | Passos / Entrada | Resultado esperado |
|---|---------|-------------------|---------------------|
| TC01 | Cadastrar aluno com sucesso | Menu Alunos → 1 → CPF `11111111111`, nome, email, telefone | "Aluno cadastrado com sucesso!" |
| TC02 | **Exceção:** CPF duplicado | Repetir TC01 com o mesmo CPF `11111111111` | `AlunoJaCadastradoException` → mensagem de erro amigável, sistema continua rodando |
| TC03 | Buscar aluno existente | Menu Alunos → 2 → CPF `11111111111` | Exibe os dados do aluno em tabela |
| TC04 | **Exceção:** buscar aluno inexistente | Menu Alunos → 2 → CPF `99999999999` (nunca cadastrado) | `AlunoNaoEncontradoException` → mensagem de erro, sem crash |
| TC05 | Listar alunos | Menu Alunos → 3 (antes de cadastrar qualquer um, e depois de cadastrar 2+) | Vazio: "Nenhum aluno cadastrado." · Com dados: tabela alinhada com todos os alunos |

---

## Módulo 2 — Ambientes

| # | Cenário | Passos / Entrada | Resultado esperado |
|---|---------|-------------------|---------------------|
| TC06 | Cadastrar um ambiente de cada tipo | Menu Ambientes → 1, repetir para tipos 1 (Musculação), 2 (Yoga), 3 (Crossfit), 4 (Piscina), com IDs `A1`, `A2`, `A3`, `A4` | "Ambiente cadastrado com sucesso!" para cada um |
| TC07 | **Exceção:** ID de ambiente duplicado | Repetir cadastro usando ID `A1` de novo | `AmbienteJaCadastradoException` → mensagem "Já existe um ambiente cadastrado com o ID 'A1'." |
| TC08 | Listar ambientes | Menu Ambientes → 2 | Tabela com Tipo, ID, Nome e Valor/Hora de todos os cadastrados |

---

## Módulo 3 — Agendamentos e Serviços

| # | Cenário | Passos / Entrada | Resultado esperado |
|---|---------|-------------------|---------------------|
| TC09 | Criar agendamento sem serviços | Menu Agendamentos → 1 → CPF `11111111111`, ambiente `A1`, data futura, `08:00`–`09:00`, depois `0` (finalizar sem serviço) | Agendamento criado, valor = 1h × valor/hora do ambiente |
| TC10 | Criar agendamento com serviços adicionais | Repetir TC09 escolhendo 1 ou mais serviços (avaliação física, nutricionista, personal trainer, locker com quantidade) antes de finalizar | Valor total = horas × valor/hora + soma dos serviços — confira a conta na tela |
| TC11 | **Exceção:** aluno inexistente | Menu Agendamentos → 1 → CPF `00000000000` (não cadastrado) | `AlunoNaoEncontradoException` → mensagem de erro, operação cancelada |
| TC12 | **Exceção:** ambiente inexistente | Menu Agendamentos → 1 → CPF válido, ID de ambiente `Z9` (não cadastrado) | Erro "Ambiente com ID 'Z9' não encontrado." (`IllegalArgumentException`, exceção padrão do Java — não é uma das 6 customizadas, mas vale mencionar na arguição) |
| TC13 | **Exceção:** horário sobreposto | Criar novo agendamento para o mesmo ambiente `A1`, mesma data do TC09, horário que se sobrepõe (ex.: `08:30`–`09:30`) | `AmbienteIndisponivelException` → "Ambiente já reservado das 08:00 às 09:00." |
| TC14 | **Exceção:** opção de serviço inválida | Durante a seleção de serviços (TC09/TC10), digite uma opção fora do menu (ex.: `9`) | `ServicoInvalidoException` → "Opção de serviço inexistente."; operação de criação é cancelada |
| TC15 | Adicionar serviço a reserva existente | Menu Agendamentos → 3 → ID de uma reserva já criada, escolha um serviço | "Serviço adicionado com sucesso à reserva #N."; valor total da reserva é atualizado |
| TC16 | **Exceção:** ID de reserva inexistente | Menu Agendamentos → 3 → ID `9999` (não existe) | `AgendamentoNaoEncontradoException` → mensagem de erro |
| TC17 | **Exceção:** serviço inválido ao adicionar | Menu Agendamentos → 3 → ID válido, opção de serviço `9` | `ServicoInvalidoException` → mensagem de erro |
| TC18 | Cancelar agendamento | Menu Agendamentos → 2 → ID de uma reserva existente | "Agendamento #N cancelado com sucesso." |
| TC19 | **Exceção:** cancelar reserva inexistente | Menu Agendamentos → 2 → ID `9999` | `AgendamentoNaoEncontradoException` → mensagem de erro |
| TC20 | Listar agendamentos | Menu Agendamentos → 4 | Tabela com ID, Aluno, Ambiente, Data, Horário e Total de cada agendamento ativo |

---

## Módulo 4 — Relatórios

| # | Cenário | Passos / Entrada | Resultado esperado |
|---|---------|-------------------|---------------------|
| TC21 | Relatório por aluno — com dados | Menu Relatórios → 1 → CPF de um aluno com reservas | Lista as reservas daquele aluno em tabela |
| TC21b | Relatório por aluno — sem dados | Menu Relatórios → 1 → CPF de um aluno sem reservas | "Nenhum agendamento encontrado para este aluno." |
| TC22 | Relatório de utilização de ambientes | Menu Relatórios → 2 | Tabela: ambiente, quantidade de agendamentos, total de horas usadas |
| TC23 | Relatório de faturamento | Menu Relatórios → 3 | Três blocos (por dia, por ambiente, por aluno) + total geral, todos batendo com os agendamentos criados |
| TC24 | Relatório de serviços adicionais | Menu Relatórios → 4 | Tabela: tipo de serviço, quantidade vendida, valor total arrecadado |

---

## Resumo — onde cada exceção é disparada

| Exceção | Onde testar | Caso(s) |
|---|---|---|
| `AlunoJaCadastradoException` | Menu Alunos → Cadastrar, com CPF repetido | TC02 |
| `AlunoNaoEncontradoException` | Menu Alunos → Buscar, ou Agendamentos → Nova reserva, com CPF inexistente | TC04, TC11 |
| `AmbienteJaCadastradoException` | Menu Ambientes → Cadastrar, com ID repetido | TC07 |
| `AmbienteIndisponivelException` | Agendamentos → Nova reserva, com horário sobreposto no mesmo ambiente/data | TC13 |
| `AgendamentoNaoEncontradoException` | Agendamentos → Adicionar serviço ou Cancelar, com ID inexistente | TC16, TC19 |
| `ServicoInvalidoException` | Seleção de serviço (na criação ou ao adicionar depois), com opção fora do menu | TC14, TC17 |

`FalhaPersistenciaException` não está neste roteiro por exigir simular corrupção/indisponibilidade do arquivo `.dat` — se quiser testar depois, um jeito simples é fechar o programa, editar manualmente um dos arquivos `.dat` num editor de texto (corrompendo o conteúdo) e tentar abrir o sistema de novo.

---

## Sugestão de ordem de execução

Para não perder dados de um teste pro outro, rode nesta ordem numa mesma sessão: **Alunos (TC01–TC05) → Ambientes (TC06–TC08) → Agendamentos (TC09–TC20) → Relatórios (TC21–TC24)**. Assim os relatórios já têm dados reais pra mostrar, e você testa as exceções de "não encontrado" antes de cadastrar o que falta, e as de "duplicado" depois.
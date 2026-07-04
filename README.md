# FitZone — Sistema de Gestão de Academia

Projeto desenvolvido a princípio para revisar conceitos da Programação Orientada a Objetos, simulando a gestão de uma academia/estúdio fitness: cadastro de alunos, ambientes reserváveis (sala de musculação, yoga, crossfit, piscina), agendamentos com serviços adicionais (avaliação física, nutricionista, personal trainer, locker) e relatórios de ocupação e faturamento.

Este README documenta o estado atual da v1 e um roadmap de evolução gradual do projeto, já pensando além da revisão.

---

## 1. Estado atual (v1 — entrega da revisão de POO)

O que já está implementado e funcionando:

- **Modelagem OO completa**: classe abstrata `Ambiente` com 4 subclasses, interface `ServicoAdicional` com 4 implementações (polimorfismo real, não decorativo).
- **Encapsulamento**: atributos privados, getters/setters, regras de cálculo dentro das próprias entidades (`Agendamento.calculaValorTotal()`).
- **Exceções customizadas** com mensagens contextuais (`AlunoJaCadastradoException`, `AmbienteIndisponivelException`, `FalhaPersistenciaException`, etc.), tratadas tanto no `AdministradorSistema` quanto nos menus.
- **Coleções genéricas**: `HashMap` como estrutura de armazenamento nos três repositórios.
- **Persistência por serialização**: `.dat` salvos automaticamente a cada alteração, usando `ObjectOutputStream`/`ObjectInputStream`.
- **Camadas separadas**: `entidades`, `controle`, `fronteira`, `excecoes` — cada uma com responsabilidade única.
- **Interface textual completa**: cadastro/busca/listagem de alunos, cadastro/listagem de ambientes, criação/cancelamento de agendamentos, adição de serviço a reserva existente, 4 relatórios — tudo com tabelas alinhadas e navegação em loop.

### Pontas soltas a fechar antes de evoluir

Pequenos ajustes que valem a pena resolver **antes** de começar a mexer em banco de dados ou interface nova, pra não carregar dívida técnica adiante:

- [ ] Decidir sobre o atributo `disponivel` do `Ambiente`: usar de verdade (ex. marcar ambiente em manutenção) ou remover de vez — hoje ele não existe mais na classe, então é só confirmar que isso não aparece como pendência no diagrama entregue.
- [ ] Criar uma exceção própria para "ID de ambiente já cadastrado" em vez de reaproveitar `AmbienteIndisponivelException` (que semanticamente é sobre disponibilidade de horário, não duplicidade de cadastro).
- [ ] Documentar um roteiro de testes manuais (checklist: reserva sobreposta, cancelamento de reserva inexistente, CPF duplicado, serviço inválido, arquivo `.dat` corrompido) — útil tanto pra você validar quanto pra apresentar na arguição.

---

## 2. Roadmap de evolução

A ideia aqui é evoluir em fases, sem misturar muita coisa nova de uma vez. Cada fase assume que a anterior está estável.

### Fase 1 — Robustez e boas práticas (curto prazo, ainda em Java puro)

- Validações de entrada mais completas na camada de fronteira: formato de CPF, datas no passado, hora fim menor que hora início, quantidade de lockers negativa etc.
- Javadoc nas classes principais (`AdministradorSistema`, `Agendamento`, `Ambiente`) — treina documentação de API, útil pra qualquer projeto futuro.
- Testes automatizados com **JUnit 5** para a camada de controle, cobrindo as regras de negócio (sobreposição de horário, cálculo de valor total, exceções lançadas nos casos certos).
- Centralizar tratamento de erro de entrada (hoje cada menu repete `try/catch` parecido) — dá pra criar um pequeno utilitário de leitura validada (`lerInteiro()`, `lerData()`) reaproveitável entre os menus.

### Fase 2 — Persistência com banco de dados relacional

Trocar a serialização em `.dat` por um banco de verdade é o passo mais natural depois de fechar a v1:

- Migrar de `ObjectOutputStream`/`ObjectInputStream` para **JDBC + MySQL**, ou já ir direto pra **JPA/Hibernate** (você já tem prática com isso).
- Modelar as tabelas: `alunos`, `ambientes` (com uma coluna de discriminação pro tipo, se for usar herança em tabela única), `agendamentos`, `servicos_adicionais`.
- Reescrever os repositórios para fazer `SELECT`/`INSERT`/`UPDATE`/`DELETE` reais, mas **mantendo a mesma assinatura de métodos** que o `AdministradorSistema` já usa — é um ótimo exercício de Repository Pattern: a camada de controle não deveria precisar mudar quase nada.
- Ponto de atenção: regras como "CPF único" e "sem sobreposição de horário", que hoje são checadas em memória, passam a poder (e dever) ser reforçadas também no banco, via `UNIQUE` e checagens antes do `INSERT`.

### Fase 3 — Melhorias de interface (ainda em texto)

- Cores ANSI no terminal para destacar mensagens de erro (vermelho) e sucesso (verde) — simples de implementar e melhora bastante a leitura.
- Validação de entrada mais amigável, com nova tentativa em vez de cancelar a operação inteira ao digitar algo errado.
- Se quiser ir além do texto puro sem sair do terminal, dá pra explorar bibliotecas como **Lanterna** ou **JLine** para menus mais ricos (setas, seleção, cores nativas).

### Fase 4 — Arquitetura em camadas "de produção" (rumo a Spring Boot)

Esse é o salto que mais aproveita o que você já vem estudando:

- `AdministradorSistema` vira uma ou mais classes `@Service`.
- Repositórios viram interfaces `Spring Data JPA` (`AlunoRepository extends JpaRepository<Aluno, String>`, por exemplo).
- Entidades ganham anotações JPA (`@Entity`, `@Id`, `@OneToMany` etc.) — praticamente o mesmo modelo, só que anotado.
- Adicionar uma camada REST (`@RestController`) — isso já deixa o sistema pronto pra, no futuro, ter um front-end separado.

### Fase 5 — Interface gráfica ou Web

Duas direções possíveis a partir da Fase 4:

- **Opção A — JavaFX**: interface desktop, mantendo tudo em Java. Boa se você quiser focar em Java puro.
- **Opção B (recomendada, alinhada com seu objetivo de full stack)**: manter o Spring Boot como API REST e construir um front-end separado (React, ou até HTML/CSS/JS simples pra começar). Isso te dá prática real de full stack — exatamente o tipo de projeto que fica bem num portfólio.

### Fase 6 — Portfólio

- Documentar o projeto com prints de tela, diagrama de classes atualizado e instruções claras de setup (esse próprio README pode evoluir pra isso).
- Organizar o histórico do Git por fase (branches ou tags tipo `v1-poo`, `v2-jdbc`, `v3-spring-api`, `v4-web`), mostrando a evolução — isso conta muito mais do que só o código final.
- Um projeto que nasce simples (CRUD + regras de negócio) e evolui até virar API + front-end é um ótimo case pra mostrar a clientes ou recrutadores que você entende o ciclo completo, não só uma parte isolada.

---

## 3. Como rodar a v1 hoje

Projeto em pacotes Java puro, sem build tool (Maven/Gradle) por enquanto.

```bash
# a partir da raiz do projeto (onde está o Main.java)
javac -encoding UTF-8 -d bin $(find . -name "*.java")   # Linux/Mac/Git Bash
java -Dfile.encoding=UTF-8 -cp bin Main
```

No Windows (cmd/PowerShell), gere a lista de arquivos antes:

```bat
dir /s /b *.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
java -Dfile.encoding=UTF-8 -cp bin Main
```

## 4. Estrutura de pastas

```
fitzone/
├── entidades/    # modelo de domínio (Aluno, Ambiente e subclasses, Agendamento, ServicoAdicional e implementações)
├── controle/     # regras de negócio (AdministradorSistema) e repositórios
├── fronteira/    # menus da interface textual + ConsoleUtil
├── excecoes/     # exceções customizadas do domínio
└── Main.java     # ponto de entrada
```
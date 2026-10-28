# FitZone — Sistema de Gestão de Academia

Projeto desenvolvido a princípio para revisar conceitos da Programação Orientada a Objetos, simulando a gestão de uma academia/estúdio fitness: cadastro de alunos, ambientes reserváveis (sala de musculação, yoga, crossfit, piscina), agendamentos com serviços adicionais (avaliação física, nutricionista, personal trainer, locker) e relatórios de ocupação e faturamento.

O projeto evoluiu em fases — de um sistema de console em Java puro até uma **API REST com Spring Boot, MySQL e Flyway** consumida por um **front-end em React**. Este README documenta o estado atual e o histórico de cada fase.

---

## 1. Estado atual (Fase 5 concluída — front-end React + API REST)

**Front-end** (`frontend/`)
- **React 19 + Vite 8 + React Router 7**, em JavaScript, conversando com a API por um cliente HTTP único (`src/api/client.js`) que transforma qualquer `ErroResposta` da API num `ApiError` com status, mensagem e campos inválidos.
- **Cinco telas**: Painel (o movimento de hoje, com o que está em andamento e os próximos agendamentos), Agendamentos (lista com filtros opcionais de data e ambiente, novo agendamento com horários ocupados e prévia do valor, detalhe com serviços e cancelamento), Alunos (busca por nome, CPF ou e-mail, cadastro e edição com máscaras), Ambientes (agrupados por tipo, cadastro, edição de observações e exclusão) e Relatórios (faturamento por dia, ocupação dos ambientes e serviços vendidos, com gráficos).
- **Nenhuma regra de cobrança no front**: a prévia do valor vem do endpoint de orçamento e os preços vêm dos endpoints de tipos — a regra de horas cobradas continua existindo só em `Agendamento.getHorasCobradas()`.
- **Identidade visual**: fonte Archivo (condensada nos títulos e horários) e as cores das anilhas olímpicas identificando cada tipo de ambiente (vermelho = musculação, verde = yoga, amarelo = crossfit, azul = piscina).
- **Acessibilidade de base**: modais com `<dialog>` nativo (foco preso, Esc), foco visível, erros ligados aos campos (`aria-invalid`, `aria-describedby`), avisos anunciados por leitores de tela, tabela alternativa nos gráficos e layout que funciona no celular.

**Back-end** (API REST)
- **API REST** com Spring Boot 4 (Java 21): CRUD de alunos e ambientes, agendamentos com serviços adicionais e 4 relatórios, em `/api/*`.
- **Arquitetura em camadas**: `domain` (modelo, repositórios, services, exceções) separado de `api` (controllers, DTOs, tratamento de erros) — as regras de negócio não sabem que existe HTTP.
- **Banco versionado com Flyway**: o schema é criado e evoluído por migrations (`src/main/resources/db/migration`); o Hibernate só valida (`ddl-auto=validate`).
- **Regras de negócio também no banco**: constraints `CHECK` (horário, valores, faixa de ids por tipo, tipos válidos, quantidade de lockers), `UNIQUE` (CPF, e-mail) e FKs com `ON DELETE RESTRICT` — última linha de defesa caso algum bug passe pelos services.
- **Modelagem OO preservada**: `Ambiente` e `ServicoAdicional` são classes abstratas com herança `SINGLE_TABLE` e polimorfismo real (cada subtipo calcula sua descrição e seu valor).
- **DTOs (records) na fronteira da API** com Bean Validation — as entidades JPA nunca são expostas no JSON.
- **Erros padronizados**: um `@RestControllerAdvice` converte cada exceção no status certo (400, 404, 405, 409, 415, 500), sempre no mesmo formato JSON, com a lista de campos inválidos quando é erro de validação.
- **Testes**: 25 testes de integração (JUnit 5 + `@SpringBootTest`) contra um banco MySQL próprio de testes (`fitzone_test`), mais uma coleção do Postman com os cenários de teste manual (`postman/` e [Roteiro.md](Roteiro.md)).
- **Credenciais fora do código**: lidas de um arquivo `.env` que não é versionado.

### Pendências conhecidas

Pontos identificados durante as Fases 4 e 5 e deixados conscientemente para depois:

- [ ] **Valores em `DOUBLE`**: `valor_hora` e `valor_total` deveriam ser `DECIMAL` (`BigDecimal` no Java) para evitar erro de arredondamento em dinheiro. Exige uma migration nova (V5) e trocar `double` nas entidades, no cálculo e nos relatórios.
- [ ] **Ids da faixa não são reaproveitados**: o próximo id de ambiente é `MAX + 1` do tipo. Se um ambiente do meio da faixa for excluído, o número não volta a ser usado — com 10 ids por tipo, a faixa pode esgotar antes da hora. Alternativa: procurar o primeiro id livre da faixa.
- [ ] **Excluir aluno/ambiente com agendamentos** devolve a mensagem genérica "Conflito de integridade" (vinda do banco). Uma checagem prévia no service (`existsByAluno_Id`) com exceção própria daria uma mensagem mais clara.
- [ ] **Relatórios agregam em memória** (`findAll()` + streams). Suficiente para o volume atual; com muitos dados, o ideal é consultas com `GROUP BY` no banco.
- [ ] **Relatórios sem filtro de período**: o faturamento por dia mostra só os dias com movimento, igualmente espaçados. Um parâmetro de datas em `/relatorios/faturamento` permitiria "últimos 30 dias" com os dias vazios aparecendo como zero.
- [ ] **Não dá para remover um serviço** de um agendamento (falta `DELETE /api/agendamentos/{id}/servicos/{servicoId}`); hoje a única saída é cancelar o agendamento.
- [ ] **Sem autenticação**: o front é um painel administrativo aberto. Login (Spring Security + JWT) fica para quando houver mais de um tipo de usuário.
- [ ] **Listas carregadas inteiras** (alunos, agendamentos) e filtradas no navegador. Ótimo para o volume de uma academia; com milhares de registros, o caminho é paginação na API.

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

### Fase 5 — Front-end web com React ✔ (concluída)

Escolhida a "Opção B": o Spring Boot continua como API REST e o front-end é um projeto separado, na pasta `frontend/` do mesmo repositório.

**Ligação front ↔ back**
- ✔ **CORS** configurado no Spring (`api/config/CorsConfig`), com as origens permitidas no `application.properties` (`fitzone.cors.origens-permitidas`) e o header `Location` exposto ao JavaScript.
- ✔ URL da API no front via `frontend/.env.development` (`VITE_API_URL`) — só endereço, nunca credencial: tudo que vai para o front é público.
- ✔ Cliente HTTP central que trata 204 sem corpo, falha de conexão (status 0) e o `ErroResposta` da API; um módulo por recurso em `src/api/`.

**Mudanças no back feitas para o front**
- ✔ `TipoAmbiente` e `TipoServico` viraram a "ficha técnica" de cada tipo (nome, preço e, nos serviços, a forma de cobrança `FIXO` / `POR_HORA` / `POR_UNIDADE`). Os preços saíram das subclasses; o cálculo continua polimórfico. Endpoints `GET /api/ambientes/tipos` e `GET /api/servicos/tipos`.
- ✔ Responses com o **código** do enum (`"YOGA"`) ao lado do texto de exibição (`"Sala de Yoga"`): o front decide pelo código, nunca pelo texto.
- ✔ Listagem de agendamentos com filtros opcionais (`?data=&ambienteId=&alunoId=`), ordenada por data e hora.
- ✔ **Orçamento** (`GET /api/agendamentos/orcamento`): prévia do valor calculada pelo próprio `Agendamento`, sem gravar nada — a regra de cobrança não foi duplicada no JavaScript.
- ✔ Relatório de ocupação com código do tipo e valor faturado por ambiente; relatório de serviços agrupado pelo enum (antes mostrava o nome da classe Java, como `LockerAcademia`).
- ✔ Corrigidos no caminho: mensagens sem espaço ("tipo YOGAatingido"), "Locker (1 unidades)" e o nome da Piscina sem número.

**Front-end**
- ✔ Layout com navegação no topo, componentes reutilizáveis (`Botao`, `Modal`, `ModalConfirmacao`, `CampoTexto`, `MensagemErro`, avisos via Context) e os hooks `useDados` (busca com recarga e descarte de respostas atrasadas) e `useAgora` (relógio que avança a cada minuto).
- ✔ As cinco telas descritas no estado atual, com filtros na URL (F5 e links mantêm o que foi escolhido).
- ✔ Datas tratadas sem `toISOString()`/`new Date("AAAA-MM-DD")`, que convertem para UTC e mostrariam o dia errado no fuso de Brasília.
- ✔ Gráficos feitos em HTML/CSS: barras de uma cor só (as cores das anilhas reprovaram no teste de daltonismo e ficaram só como marca ao lado do nome), valor no maior dia, dica ao passar o mouse ou focar com Tab e tabela alternativa.

### Fase 6 — Portfólio

- Documentar o projeto com prints de tela, diagrama de classes atualizado e instruções claras de setup (esse próprio README pode evoluir pra isso).
- Organizar o histórico do Git por fase (tags tipo `v1-poo`, `v2-jpa`, `v3-console`, `v4-spring-api`, `v5-web`), mostrando a evolução — isso conta muito mais do que só o código final.
- Um projeto que nasce simples (CRUD + regras de negócio) e evolui até virar API + front-end é um ótimo case pra mostrar a clientes ou recrutadores que você entende o ciclo completo, não só uma parte isolada.

### Ideias para o futuro (fora do roadmap atual)

Possíveis expansões, ainda sem fase definida:

- **Nova identidade visual**: rever a paleta do sistema e as cores das anilhas que identificam os tipos de ambiente (hoje vermelho, verde, amarelo e azul; o par vermelho/verde não é distinguível para daltônicos, por isso a cor nunca aparece sozinha).
- **Site institucional** da academia: apresentação, modalidades, ambientes, preços e contato, para o público.
- **Página do aluno**: o aluno acompanha os próprios agendamentos, monta os treinos e organiza a rotina.
- **Página do professor**: o professor analisa as avaliações físicas dos alunos e entrega treinos personalizados.

As três páginas novas pedem **autenticação com perfis** (recepção/gestão, aluno, professor) e regras de acesso por perfil na API — provavelmente o primeiro passo de qualquer uma delas.

---

## 3. Como rodar

**Pré-requisitos:** JDK 21, MySQL **8.0.16 ou mais novo** rodando localmente (versões anteriores aceitam as constraints `CHECK` mas não as aplicam) e, para o front-end, **Node.js 20 ou mais novo**. Não é preciso instalar o Maven: o projeto traz o Maven Wrapper (`mvnw`).

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

**5. Subir o front-end** (com a API rodando, em outro terminal):

```bash
cd frontend
npm install
npm run dev
```

O sistema abre em `http://localhost:5173`. O endereço da API está em `frontend/.env.development` (`VITE_API_URL`); se o front rodar em outra origem, ela precisa estar em `fitzone.cors.origens-permitidas`, no `application.properties`, senão o navegador bloqueia as requisições (erro de CORS no Console). Para conferir o código: `npm run lint`.

**6. Testar manualmente:** a coleção do Postman está em `postman/` e o checklist dos cenários em [Roteiro.md](Roteiro.md).

---

## 4. API

| Recurso | Rotas |
|---|---|
| Alunos | `POST /api/alunos` · `GET /api/alunos` · `GET /api/alunos/{id}` · `GET /api/alunos/busca?cpf=` · `PUT /api/alunos/{id}` · `DELETE /api/alunos/{id}` |
| Ambientes | `POST /api/ambientes` · `GET /api/ambientes` · `GET /api/ambientes/{id}` · `PUT /api/ambientes/{id}` (só observações) · `DELETE /api/ambientes/{id}` · `GET /api/ambientes/tipos` |
| Agendamentos | `POST /api/agendamentos` · `GET /api/agendamentos?data=&ambienteId=&alunoId=` (filtros opcionais) · `GET /api/agendamentos/{id}` · `GET /api/agendamentos/orcamento?ambienteId=&horaInicio=&horaFim=` · `POST /api/agendamentos/{id}/servicos` · `DELETE /api/agendamentos/{id}` |
| Serviços | `GET /api/servicos/tipos` (catálogo: nome, preço e forma de cobrança) |
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
├── frontend/                 # front-end React + Vite
│   ├── .env.development      # VITE_API_URL (endereço da API, não é segredo)
│   └── src/
│       ├── api/              # cliente HTTP central + um módulo por recurso
│       ├── avisos/           # Context dos avisos (useAviso)
│       ├── components/       # Layout, Modal, Botao, campos, gráficos, itens e modais de cada tela
│       ├── hooks/            # useDados (busca/recarga) e useAgora (relógio)
│       ├── pages/            # Painel, Agendamentos, Alunos, Ambientes, Relatorios, NaoEncontrada
│       ├── formatos.js       # moeda, CPF, telefone, datas e horas (sem armadilhas de fuso)
│       └── index.css         # tokens de cor e fonte, tabelas, campos e anilhas
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
    │   │       ├── config/           # CorsConfig (origens liberadas para o front)
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

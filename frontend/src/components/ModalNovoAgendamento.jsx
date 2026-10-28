import { useId, useState } from 'react'
import { agendamentosApi } from '../api/agendamentos'
import { alunosApi } from '../api/alunos'
import { ambientesApi } from '../api/ambientes'
import { ehDataIso, formatarCpf, formatarHora, formatarMoeda, hojeIso, plural } from '../formatos'
import { useDados } from '../hooks/useDados'
import Botao from './Botao'
import CampoTexto from './CampoTexto'
import MensagemErro from './MensagemErro'
import Modal from './Modal'
import './ModalNovoAgendamento.css'

// Espelha o AgendamentoRequest (e a regra fim > início do service) para o erro aparecer antes do envio.
// Datas "AAAA-MM-DD" e horas "HH:mm" comparadas como texto já ficam em ordem cronológica.
function validar({ alunoId, ambienteId, data, horaInicio, horaFim }, hoje) {
  const erros = {}
  if (!alunoId) erros.alunoId = 'Escolha o aluno.'
  if (!ambienteId) erros.ambienteId = 'Escolha o ambiente.'
  if (!ehDataIso(data)) erros.data = 'Informe a data.'
  else if (data < hoje) erros.data = 'Não é possível agendar em uma data passada.'
  if (!horaInicio) erros.horaInicio = 'Informe a hora de início.'
  if (!horaFim) erros.horaFim = 'Informe a hora de fim.'
  else if (horaInicio && horaFim <= horaInicio) erros.horaFim = 'A hora de fim deve ser depois da hora de início.'
  return erros
}

export default function ModalNovoAgendamento({ dataInicial, onFechar, onAgendado }) {
  const id = useId()
  const hoje = hojeIso()
  const [dados, setDados] = useState({
    alunoId: '',
    ambienteId: '',
    // se a agenda estava filtrada por um dia futuro, o formulário já começa nele
    data: ehDataIso(dataInicial) && dataInicial >= hoje ? dataInicial : hoje,
    horaInicio: '',
    horaFim: '',
  })
  const [errosCampos, setErrosCampos] = useState({})
  const [erroGeral, setErroGeral] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const { alunoId, ambienteId, data, horaInicio, horaFim } = dados

  // opções dos selects
  const opcoes = useDados(() => Promise.all([alunosApi.listar(), ambientesApi.listar()]))
  const [alunos, ambientes] = opcoes.dados ?? [[], []]
  const alunosEmOrdem = [...alunos].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
  // agrupa os ambientes por tipo para o <optgroup>; ordenar por id já deixa cada tipo junto (101–110, 201–210...)
  const gruposAmbiente = Object.values(
    [...ambientes]
      .sort((a, b) => a.id - b.id)
      .reduce((grupos, amb) => {
        grupos[amb.codigo] ??= { tipo: amb.tipo, itens: [] }
        grupos[amb.codigo].itens.push(amb)
        return grupos
      }, {}),
  )
  const ambiente = ambientes.find(a => String(a.id) === ambienteId)

  // horários já reservados do ambiente no dia: busca de novo sempre que ambiente ou data mudarem
  const temAmbienteEData = Boolean(ambienteId) && ehDataIso(data)
  const ocupados = useDados(
    () => (temAmbienteEData ? agendamentosApi.listar({ data, ambienteId }) : Promise.resolve([])),
    `${ambienteId}|${data}`,
  )
  const reservas = ocupados.dados ?? []
  // mesma condição do buscarConflitos() do back; aqui é só aviso, quem garante é o 409 da API
  const conflita = r => horaInicio && horaFim && horaInicio < formatarHora(r.horaFim) && horaFim > formatarHora(r.horaInicio)
  const temConflito = reservas.some(conflita)

  // prévia do valor calculada pela API: a regra de horas cobradas não é repetida aqui
  const horarioCompleto = Boolean(ambienteId && horaInicio && horaFim && horaFim > horaInicio)
  const orcamento = useDados(
    () => (horarioCompleto ? agendamentosApi.orcar(ambienteId, horaInicio, horaFim) : Promise.resolve(null)),
    `${ambienteId}|${horaInicio}|${horaFim}`,
  )

  const alterar = campo => valor => {
    setDados(atual => ({ ...atual, [campo]: valor }))
    setErrosCampos(atual => ({ ...atual, [campo]: undefined }))
  }

  async function enviar(evento) {
    evento.preventDefault()

    const erros = validar(dados, hoje)
    const primeiroComErro = Object.keys(erros)[0]
    if (primeiroComErro) {
      setErrosCampos(erros)
      document.getElementById(`${id}-${primeiroComErro}`)?.focus()
      return
    }

    setEnviando(true)
    setErrosCampos({})
    setErroGeral(null)

    try {
      const criado = await agendamentosApi.realizar({
        alunoId: Number(alunoId),
        ambienteId: Number(ambienteId),
        data,
        horaInicio,
        horaFim,
      })
      onAgendado(criado)
    } catch (erro) {
      if (erro.campos.length > 0) {
        setErrosCampos(Object.fromEntries(erro.campos.map(c => [c.nome, c.mensagem])))
      } else {
        // 409 "Ambiente já reservado das 08:00 às 10:40." (alguém reservou enquanto o formulário estava aberto),
        // 404 de aluno/ambiente excluído, servidor fora...
        setErroGeral(erro)
        ocupados.recarregar()
      }
      setEnviando(false)
    }
  }

  const semOpcoes = opcoes.dados && (alunos.length === 0 || ambientes.length === 0)

  return (
    <Modal onFechar={onFechar} bloqueado={enviando} tituloId={`${id}-titulo`} className="modal-largo">
      <h2 id={`${id}-titulo`}>Novo agendamento</h2>

      {opcoes.carregando && !opcoes.dados && <p className="estado-carregando">Carregando alunos e ambientes…</p>}
      {opcoes.erro && <MensagemErro erro={opcoes.erro} onTentarDeNovo={opcoes.recarregar} />}

      {semOpcoes && (
        <>
          <p className="modal-mensagem">
            {alunos.length === 0
              ? 'Cadastre um aluno antes de fazer um agendamento.'
              : 'Cadastre um ambiente antes de fazer um agendamento.'}
          </p>
          <div className="modal-acoes">
            <Botao variante="secundario" onClick={onFechar}>Voltar</Botao>
          </div>
        </>
      )}

      {opcoes.dados && !semOpcoes && (
        <form onSubmit={enviar} noValidate>
          {erroGeral && (
            <div style={{ marginBottom: 20 }}>
              <MensagemErro erro={erroGeral} />
            </div>
          )}

          <div className="campo">
            <label htmlFor={`${id}-alunoId`}>Aluno</label>
            {/* select nativo: digitar as primeiras letras do nome já pula para ele */}
            <select
              id={`${id}-alunoId`}
              value={alunoId}
              onChange={e => alterar('alunoId')(e.target.value)}
              aria-invalid={Boolean(errosCampos.alunoId)}
              aria-describedby={errosCampos.alunoId ? `${id}-alunoId-erro` : undefined}
            >
              <option value="" disabled>Escolha o aluno</option>
              {alunosEmOrdem.map(a => (
                <option key={a.id} value={a.id}>{`${a.nome} (${formatarCpf(a.cpf)})`}</option>
              ))}
            </select>
            {errosCampos.alunoId && <p id={`${id}-alunoId-erro`} className="campo-erro">{errosCampos.alunoId}</p>}
          </div>

          <div className="campo">
            <label htmlFor={`${id}-ambienteId`}>Ambiente</label>
            <select
              id={`${id}-ambienteId`}
              value={ambienteId}
              onChange={e => alterar('ambienteId')(e.target.value)}
              aria-invalid={Boolean(errosCampos.ambienteId)}
              aria-describedby={errosCampos.ambienteId ? `${id}-ambienteId-erro` : undefined}
            >
              <option value="" disabled>Escolha o ambiente</option>
              {gruposAmbiente.map(g => (
                <optgroup key={g.tipo} label={g.tipo}>
                  {g.itens.map(amb => (
                    <option key={amb.id} value={amb.id}>
                      {`${amb.nome}, ${formatarMoeda(amb.valorHora)} por hora`}
                    </option>
                  ))}
                </optgroup>
              ))}
            </select>
            {errosCampos.ambienteId && (
              <p id={`${id}-ambienteId-erro`} className="campo-erro">{errosCampos.ambienteId}</p>
            )}
          </div>

          <div className="linha-campos">
            <CampoTexto
              id={`${id}-data`}
              rotulo="Data"
              type="date"
              min={hoje}
              valor={data}
              onChange={alterar('data')}
              erro={errosCampos.data}
            />
            <CampoTexto
              id={`${id}-horaInicio`}
              rotulo="Início"
              type="time"
              valor={horaInicio}
              onChange={alterar('horaInicio')}
              erro={errosCampos.horaInicio}
            />
            <CampoTexto
              id={`${id}-horaFim`}
              rotulo="Fim"
              type="time"
              valor={horaFim}
              onChange={alterar('horaFim')}
              erro={errosCampos.horaFim}
            />
          </div>

          {temAmbienteEData && (
            <div className="ocupados" aria-live="polite" aria-busy={ocupados.carregando}>
              <p className="ocupados-titulo">Horários já reservados em {ambiente?.nome} neste dia</p>
              {ocupados.erro ? (
                <p>{ocupados.erro.message}</p>
              ) : ocupados.carregando && !ocupados.dados ? (
                <p>Consultando…</p>
              ) : reservas.length === 0 ? (
                <p>Nenhum. O ambiente está livre o dia todo.</p>
              ) : (
                <ul className="numero">
                  {reservas.map(r => (
                    <li key={r.id} className={conflita(r) ? 'conflita' : undefined}>
                      {formatarHora(r.horaInicio)} às {formatarHora(r.horaFim)}
                      {conflita(r) && ', em conflito com o horário escolhido'}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}

          {horarioCompleto && (
            // a borda veste a anilha do tipo escolhido
            <div className="previa" data-tipo={ambiente?.codigo} aria-live="polite" aria-busy={orcamento.carregando}>
              {orcamento.erro ? (
                <p>{orcamento.erro.message}</p>
              ) : orcamento.dados ? (
                <>
                  <p className="previa-valor numero">{formatarMoeda(orcamento.dados.valorTotal)}</p>
                  <p className="previa-conta numero">
                    {plural(orcamento.dados.horasCobradas, 'hora cobrada', 'horas cobradas')} ×{' '}
                    {formatarMoeda(orcamento.dados.valorHora)}
                  </p>
                  <p className="previa-regra">
                    Só horas completas são cobradas, com mínimo de 1 hora. Serviços como personal trainer e
                    locker são adicionados depois, no agendamento.
                  </p>
                </>
              ) : (
                <p>Calculando…</p>
              )}
            </div>
          )}

          <div className="modal-acoes">
            <Botao variante="secundario" onClick={onFechar} disabled={enviando}>Cancelar</Botao>
            <Botao type="submit" disabled={enviando || temConflito}>
              {enviando ? 'Confirmando…' : 'Confirmar agendamento'}
            </Botao>
          </div>
        </form>
      )}
    </Modal>
  )
}

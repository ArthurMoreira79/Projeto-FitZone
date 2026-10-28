import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { agendamentosApi } from '../api/agendamentos'
import { ambientesApi } from '../api/ambientes'
import { useAviso } from '../avisos/AvisoContext'
import Botao from '../components/Botao'
import CabecalhoPagina from '../components/CabecalhoPagina'
import MensagemErro from '../components/MensagemErro'
import ModalNovoAgendamento from '../components/ModalNovoAgendamento'
import { ehDataIso, formatarData, formatarHora, formatarMoeda, hojeIso, plural } from '../formatos'
import { useDados } from '../hooks/useDados'
import './Agendamentos.css'

export default function Agendamentos() {
  // filtros na URL (?data=2026-12-01&tipo=YOGA): F5, voltar e link compartilhado mantêm o que foi escolhido
  const [params, setParams] = useSearchParams()
  const data = ehDataIso(params.get('data')) ? params.get('data') : ''
  const tipo = params.get('tipo') ?? ''

  // carrega todos de uma vez: os filtros rodam no navegador, sem nova requisição a cada escolha
  const agenda = useDados(agendamentosApi.listar)
  const tipos = useDados(ambientesApi.listarTipos)
  const [novoAberto, setNovoAberto] = useState(false)
  const avisar = useAviso()

  function aoAgendar(criado) {
    setNovoAberto(false)
    avisar(`Agendamento confirmado: ${criado.ambienteNome}, ${formatarData(criado.data)} às ${formatarHora(criado.horaInicio)}.`)
    agenda.recarregar()
  }

  function mudarFiltro(chave, valor) {
    setParams(
      atuais => {
        const novos = new URLSearchParams(atuais)
        if (valor) novos.set(chave, valor)
        else novos.delete(chave)
        return novos
      },
      { replace: true },
    )
  }

  const todos = agenda.dados ?? []
  // ordem de horário: data e depois hora de início. As strings ISO ("2026-12-01", "08:00:00")
  // comparadas como texto já ficam em ordem cronológica, por isso basta o localeCompare
  const emOrdem = [...todos].sort((a, b) =>
    a.data.localeCompare(b.data) || a.horaInicio.localeCompare(b.horaInicio))
  const filtrados = emOrdem.filter(ag => (!data || ag.data === data) && (!tipo || ag.ambienteCodigo === tipo))
  const filtrando = Boolean(data || tipo)
  const totalFiltrado = filtrados.reduce((soma, ag) => soma + ag.valorTotal, 0)

  const resumo = filtrando
    ? `${filtrados.length} de ${plural(todos.length, 'agendamento', 'agendamentos')}, ${formatarMoeda(totalFiltrado)}`
    : `${plural(todos.length, 'agendamento', 'agendamentos')}, ${formatarMoeda(totalFiltrado)}`

  return (
    <>
      <CabecalhoPagina
        titulo="Agendamentos"
        descricao="Reserve horários e adicione serviços como personal trainer e locker."
      >
        <Botao onClick={() => setNovoAberto(true)}>Novo agendamento</Botao>
      </CabecalhoPagina>

      <div className="agenda-filtros">
        <div className="campo">
          <label htmlFor="filtro-data">Data</label>
          <input id="filtro-data" type="date" value={data} onChange={e => mudarFiltro('data', e.target.value)} />
        </div>

        <div className="campo">
          <label htmlFor="filtro-tipo">Ambiente</label>
          <select
            id="filtro-tipo"
            value={tipo}
            onChange={e => mudarFiltro('tipo', e.target.value)}
            disabled={!tipos.dados}
          >
            <option value="">Todos os ambientes</option>
            {(tipos.dados ?? []).map(t => (
              <option key={t.codigo} value={t.codigo}>{t.nome}</option>
            ))}
          </select>
        </div>

        <div className="agenda-filtros-acoes">
          <Botao variante="secundario" onClick={() => mudarFiltro('data', hojeIso())} disabled={data === hojeIso()}>
            Hoje
          </Botao>
          {/* sem resultado, quem oferece "Limpar filtros" é a mensagem de lista vazia */}
          {filtrando && filtrados.length > 0 && (
            <Botao variante="discreto" onClick={() => setParams({}, { replace: true })}>Limpar filtros</Botao>
          )}
        </div>
      </div>

      {agenda.dados && !agenda.erro && filtrados.length > 0 && (
        <p className="agenda-resumo numero" aria-live="polite">{resumo}</p>
      )}

      {agenda.carregando && !agenda.dados && <p className="estado-carregando">Carregando agendamentos…</p>}

      {agenda.erro && <MensagemErro erro={agenda.erro} onTentarDeNovo={agenda.recarregar} />}

      {!agenda.erro && agenda.dados && todos.length === 0 && (
        <p className="lista-vazia">Nenhum agendamento feito ainda.</p>
      )}

      {!agenda.erro && todos.length > 0 && filtrados.length === 0 && (
        <div className="lista-vazia">
          <p>Nenhum agendamento com esses filtros.</p>
          <Botao variante="secundario" onClick={() => setParams({}, { replace: true })}>Limpar filtros</Botao>
        </div>
      )}

      {!agenda.erro && filtrados.length > 0 && (
        <ol className="agenda-lista" aria-busy={agenda.carregando}>
          {filtrados.map(ag => (
            <li key={ag.id} className="agenda-item" data-tipo={ag.ambienteCodigo}>
              <div className="agenda-quando numero">
                <span className="agenda-data">{formatarData(ag.data)}</span>
                <span className="agenda-inicio">{formatarHora(ag.horaInicio)}</span>
                <span className="agenda-fim">até {formatarHora(ag.horaFim)}</span>
              </div>

              <span className="anilha" aria-hidden="true" />

              <div className="agenda-detalhes">
                <p className="agenda-ambiente">{ag.ambienteNome}</p>
                <p>{ag.alunoNome}</p>
                <p className="agenda-extra">
                  {plural(ag.horasCobradas, 'hora cobrada', 'horas cobradas')}
                  {ag.servicos.length > 0 && `, com ${ag.servicos.map(s => s.descricao).join(', ')}`}
                </p>
              </div>

              <p className="agenda-valor numero">{formatarMoeda(ag.valorTotal)}</p>
            </li>
          ))}
        </ol>
      )}

      {novoAberto && (
        <ModalNovoAgendamento dataInicial={data} onFechar={() => setNovoAberto(false)} onAgendado={aoAgendar} />
      )}
    </>
  )
}

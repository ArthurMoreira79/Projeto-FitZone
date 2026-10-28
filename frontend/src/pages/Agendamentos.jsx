import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { agendamentosApi } from '../api/agendamentos'
import { ambientesApi } from '../api/ambientes'
import { useAviso } from '../avisos/AvisoContext'
import Botao from '../components/Botao'
import CabecalhoPagina from '../components/CabecalhoPagina'
import ItemAgenda from '../components/ItemAgenda'
import MensagemErro from '../components/MensagemErro'
import ModalDetalheAgendamento from '../components/ModalDetalheAgendamento'
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
  // o agendamento clicado na lista; null = detalhe fechado
  const [detalhe, setDetalhe] = useState(null)
  const avisar = useAviso()

  function aoCancelar(cancelado) {
    setDetalhe(null)
    avisar(`Agendamento cancelado: ${cancelado.ambienteNome}, ${formatarData(cancelado.data)} às ${formatarHora(cancelado.horaInicio)}.`)
    agenda.recarregar()
  }

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
            <li key={ag.id}>
              <ItemAgenda agendamento={ag} onAbrir={setDetalhe} />
            </li>
          ))}
        </ol>
      )}

      {novoAberto && (
        <ModalNovoAgendamento dataInicial={data} onFechar={() => setNovoAberto(false)} onAgendado={aoAgendar} />
      )}

      {detalhe && (
        <ModalDetalheAgendamento
          agendamento={detalhe}
          onFechar={() => setDetalhe(null)}
          onAlterado={agenda.recarregar}
          onCancelado={aoCancelar}
        />
      )}
    </>
  )
}

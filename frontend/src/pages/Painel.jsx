import { useState } from 'react'
import { Link } from 'react-router'
import { agendamentosApi } from '../api/agendamentos'
import { ambientesApi } from '../api/ambientes'
import { useAviso } from '../avisos/AvisoContext'
import Botao from '../components/Botao'
import CabecalhoPagina from '../components/CabecalhoPagina'
import ItemAgenda from '../components/ItemAgenda'
import MensagemErro from '../components/MensagemErro'
import ModalDetalheAgendamento from '../components/ModalDetalheAgendamento'
import ModalNovoAgendamento from '../components/ModalNovoAgendamento'
import { formatarData, formatarDataLonga, formatarHora, formatarMoeda, hojeIso, horaIso } from '../formatos'
import { useAgora } from '../hooks/useAgora'
import { useDados } from '../hooks/useDados'
import './Painel.css'

const PROXIMOS = 5

const emOrdem = (a, b) => a.data.localeCompare(b.data) || a.horaInicio.localeCompare(b.horaInicio)

export default function Painel() {
  const { dados, carregando, erro, recarregar } = useDados(() =>
    Promise.all([agendamentosApi.listar(), ambientesApi.listar()]),
  )
  const [agendamentos, ambientes] = dados ?? [[], []]
  const [novoAberto, setNovoAberto] = useState(false)
  const [detalhe, setDetalhe] = useState(null)
  const avisar = useAviso()

  // "hoje" e "agora" vêm do mesmo relógio, que avança a cada minuto
  const agora = useAgora()
  const hoje = hojeIso(agora)
  const horaAgora = horaIso(agora)

  const deHoje = agendamentos.filter(a => a.data === hoje).sort(emOrdem)
  const proximos = agendamentos.filter(a => a.data > hoje).sort(emOrdem).slice(0, PROXIMOS)

  // strings "HH:mm" comparadas como texto já ficam em ordem de horário
  function situacao(ag) {
    if (formatarHora(ag.horaFim) <= horaAgora) return 'encerrado'
    if (formatarHora(ag.horaInicio) <= horaAgora) return 'andamento'
    return undefined
  }

  const emAndamento = deHoje.filter(a => situacao(a) === 'andamento')
  const ambientesEmUso = new Set(emAndamento.map(a => a.ambienteId)).size
  const faturamentoHoje = deHoje.reduce((soma, a) => soma + a.valorTotal, 0)

  function aoAgendar(criado) {
    setNovoAberto(false)
    avisar(`Agendamento confirmado: ${criado.ambienteNome}, ${formatarData(criado.data)} às ${formatarHora(criado.horaInicio)}.`)
    recarregar()
  }

  function aoCancelar(cancelado) {
    setDetalhe(null)
    avisar(`Agendamento cancelado: ${cancelado.ambienteNome}, ${formatarData(cancelado.data)} às ${formatarHora(cancelado.horaInicio)}.`)
    recarregar()
  }

  return (
    <>
      <CabecalhoPagina titulo="Painel" descricao="O movimento de hoje e os próximos agendamentos.">
        <Botao onClick={() => setNovoAberto(true)}>Novo agendamento</Botao>
      </CabecalhoPagina>

      {carregando && !dados && <p className="estado-carregando">Carregando o painel…</p>}
      {erro && <MensagemErro erro={erro} onTentarDeNovo={recarregar} />}

      {dados && !erro && (
        <div className="painel" aria-busy={carregando}>
          <section className="painel-indicadores" aria-label="Hoje em números">
            <div className="indicador">
              <p className="indicador-rotulo">Agendamentos hoje</p>
              <p className="indicador-valor">{deHoje.length}</p>
            </div>
            <div className="indicador">
              <p className="indicador-rotulo">Em uso agora</p>
              <p className="indicador-valor">
                {ambientesEmUso} <span className="indicador-de">de {ambientes.length} ambientes</span>
              </p>
            </div>
            <div className="indicador">
              <p className="indicador-rotulo">Faturamento de hoje</p>
              <p className="indicador-valor">{formatarMoeda(faturamentoHoje)}</p>
            </div>
          </section>

          <section aria-labelledby="painel-hoje">
            <header className="painel-secao-cabecalho">
              <h2 id="painel-hoje" className="painel-hoje-titulo">{formatarDataLonga(hoje)}</h2>
              <p className="painel-agora numero">Agora são {horaAgora}</p>
            </header>
            {deHoje.length === 0 ? (
              <p className="lista-vazia">Nenhum horário reservado para hoje.</p>
            ) : (
              <ol className="agenda-lista">
                {deHoje.map(ag => (
                  <li key={ag.id}>
                    <ItemAgenda agendamento={ag} onAbrir={setDetalhe} situacao={situacao(ag)} mostrarData={false} />
                  </li>
                ))}
              </ol>
            )}
          </section>

          <section aria-labelledby="painel-proximos">
            <header className="painel-secao-cabecalho">
              <h2 id="painel-proximos">Próximos agendamentos</h2>
              <Link to="/agendamentos" className="painel-ver-todos">Ver todos os agendamentos</Link>
            </header>
            {proximos.length === 0 ? (
              <p className="lista-vazia">Nenhum agendamento depois de hoje.</p>
            ) : (
              <ol className="agenda-lista">
                {proximos.map(ag => (
                  <li key={ag.id}>
                    <ItemAgenda agendamento={ag} onAbrir={setDetalhe} />
                  </li>
                ))}
              </ol>
            )}
          </section>
        </div>
      )}

      {novoAberto && (
        <ModalNovoAgendamento onFechar={() => setNovoAberto(false)} onAgendado={aoAgendar} />
      )}

      {detalhe && (
        <ModalDetalheAgendamento
          agendamento={detalhe}
          onFechar={() => setDetalhe(null)}
          onAlterado={recarregar}
          onCancelado={aoCancelar}
        />
      )}
    </>
  )
}

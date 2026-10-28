import { formatarData, formatarHora, formatarMoeda, plural } from '../formatos'
import './ItemAgenda.css'

const SELOS = { andamento: 'Em andamento', encerrado: 'Encerrado' }

// Um agendamento da lista. A linha inteira é um botão que abre o detalhe.
// Dentro de <button> só cabe texto "em linha", por isso os pedaços são <span> (display: block no CSS).
// situacao (opcional): 'andamento' | 'encerrado' — o Painel usa para os agendamentos de hoje.
export default function ItemAgenda({ agendamento: ag, onAbrir, situacao, mostrarData = true }) {
  return (
    <button
      type="button"
      className="agenda-item"
      data-tipo={ag.ambienteCodigo}
      data-situacao={situacao}
      onClick={() => onAbrir(ag)}
    >
      <span className="agenda-quando numero">
        {mostrarData && <span className="agenda-data">{formatarData(ag.data)}</span>}
        <span className="agenda-inicio">{formatarHora(ag.horaInicio)}</span>
        <span className="agenda-fim">até {formatarHora(ag.horaFim)}</span>
      </span>

      <span className="anilha" aria-hidden="true" />

      <span className="agenda-detalhes">
        <span className="agenda-ambiente">{ag.ambienteNome}</span>
        <span>{ag.alunoNome}</span>
        <span className="agenda-extra">
          {plural(ag.horasCobradas, 'hora cobrada', 'horas cobradas')}
          {ag.servicos.length > 0 && `, com ${ag.servicos.map(s => s.descricao).join(', ')}`}
        </span>
        {situacao && <span className="agenda-selo">{SELOS[situacao]}</span>}
      </span>

      <span className="agenda-valor numero">{formatarMoeda(ag.valorTotal)}</span>
    </button>
  )
}

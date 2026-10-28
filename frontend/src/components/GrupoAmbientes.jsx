import { formatarMoeda } from '../formatos'
import './GrupoAmbientes.css'

export default function GrupoAmbientes({ tipo, salas, capacidade }) {
  const tituloId = `grupo-${tipo.codigo}`

  return (
    <section className="grupo-ambientes" data-tipo={tipo.codigo} aria-labelledby={tituloId}>
      <header className="grupo-cabecalho">
        <span className="anilha" aria-hidden="true" />
        <div>
          <h2 id={tituloId}>{tipo.nome}</h2>
          <p className="grupo-resumo">
            <span className="numero">{salas.length} de {capacidade}</span> ambientes cadastrados,{' '}
            {formatarMoeda(tipo.valorHora)} por hora
          </p>
        </div>
      </header>

      {salas.length === 0 ? (
        <p className="grupo-vazio">Nenhum ambiente deste tipo cadastrado.</p>
      ) : (
        <div className="tabela-rolagem">
          <table className="tabela">
            {/* larguras fixas: as tabelas dos quatro tipos ficam com as colunas alinhadas */}
            <colgroup>
              <col className="col-id" />
              <col className="col-nome" />
              <col className="col-valor" />
              <col />
            </colgroup>
            
            <thead>
              <tr>
                <th scope="col">Id</th>
                <th scope="col">Nome</th>
                <th scope="col" className="col-numero">Valor/hora</th>
                <th scope="col">Observações</th>
              </tr>
            </thead>
            <tbody>
              {salas.map(sala => (
                <tr key={sala.id}>
                  <td className="numero">{sala.id}</td>
                  <td>{sala.nome}</td>
                  {/* valor da própria sala, não do tipo: é ele que o agendamento cobra */}
                  <td className="col-numero numero">{formatarMoeda(sala.valorHora)}</td>
                  <td>{sala.observacoes || <span className="vazio-tracinho">—</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
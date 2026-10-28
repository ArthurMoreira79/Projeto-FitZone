import { formatarMoeda } from '../formatos'
import Botao from './Botao'
import './GrupoAmbientes.css'

export default function GrupoAmbientes({ tipo, salas, capacidade, onEditar, onExcluir }) {
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
              <col className="col-acoes" />
            </colgroup>

            <thead>
              <tr>
                <th scope="col">Id</th>
                <th scope="col">Nome</th>
                <th scope="col" className="col-numero">Valor/hora</th>
                <th scope="col">Observações</th>
                {/* título invisível: os botões já dizem o que fazem, mas o leitor de tela precisa do nome da coluna */}
                <th scope="col"><span className="visualmente-oculto">Ações</span></th>
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
                  <td>
                    <div className="acoes-linha">
                      {/* aria-label completo: o leitor de tela diz "Editar Sala de Yoga 201", não só "Editar" */}
                      <Botao
                        variante="discreto"
                        tamanho="pequeno"
                        onClick={() => onEditar(sala)}
                        aria-label={`Editar ${sala.nome}`}
                      >
                        Editar
                      </Botao>
                      <Botao
                        variante="discreto-perigo"
                        tamanho="pequeno"
                        onClick={() => onExcluir(sala)}
                        aria-label={`Excluir ${sala.nome}`}
                      >
                        Excluir
                      </Botao>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}

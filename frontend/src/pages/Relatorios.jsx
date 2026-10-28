import { relatoriosApi } from '../api/relatorios'
import CabecalhoPagina from '../components/CabecalhoPagina'
import GraficoBarras from '../components/GraficoBarras'
import GraficoColunas from '../components/GraficoColunas'
import MensagemErro from '../components/MensagemErro'
import { formatarData, formatarMoeda, formatarMoedaCurta, plural } from '../formatos'
import { useDados } from '../hooks/useDados'
import './Relatorios.css'

const horas = h => plural(h, 'hora', 'horas')

export default function Relatorios() {
  const { dados, carregando, erro, recarregar } = useDados(() =>
    Promise.all([relatoriosApi.faturamento(), relatoriosApi.porAmbiente(), relatoriosApi.porServico()]),
  )
  const [faturamento, ocupacao, servicos] = dados ?? [null, [], []]

  // { "2026-12-01": 315 } → itens do gráfico, em ordem de data (a chave ISO ordena como texto)
  const dias = Object.entries(faturamento?.porDia ?? {})
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([dia, valor]) => ({
      chave: dia,
      rotulo: `${dia.slice(8, 10)}/${dia.slice(5, 7)}`,
      rotuloLongo: formatarData(dia),
      valor,
    }))

  const totalAgendamentos = ocupacao.reduce((soma, o) => soma + o.quantidadeAgendamentos, 0)
  const totalHoras = ocupacao.reduce((soma, o) => soma + o.horasCobradas, 0)
  // comparar magnitudes: do mais ocupado para o menos; empate (ex.: os zeros) fica na ordem do id
  const ocupacaoOrdenada = [...ocupacao].sort((a, b) => b.horasCobradas - a.horasCobradas || a.ambienteId - b.ambienteId)
  const servicosOrdenados = [...servicos].sort((a, b) => b.valorTotal - a.valorTotal)

  return (
    <>
      <CabecalhoPagina
        titulo="Relatórios"
        descricao="Faturamento por dia, ocupação de cada ambiente e serviços vendidos."
      />

      {carregando && !dados && <p className="estado-carregando">Carregando relatórios…</p>}
      {erro && <MensagemErro erro={erro} onTentarDeNovo={recarregar} />}

      {dados && !erro && (
        <div className="relatorios" aria-busy={carregando}>
          {/* o número que a tela lidera é um só: o faturamento total; os outros dois são apoio */}
          <section className="indicadores" aria-label="Resumo">
            <div className="indicador indicador-principal">
              <p className="indicador-rotulo">Faturamento total</p>
              <p className="indicador-valor">{formatarMoeda(faturamento.total)}</p>
            </div>
            <div className="indicador">
              <p className="indicador-rotulo">Agendamentos</p>
              <p className="indicador-valor">{totalAgendamentos}</p>
            </div>
            <div className="indicador">
              <p className="indicador-rotulo">Horas cobradas</p>
              <p className="indicador-valor">{totalHoras}</p>
            </div>
          </section>

          <section className="relatorio" aria-labelledby="rel-dia">
            <h2 id="rel-dia">Faturamento por dia</h2>
            <p className="relatorio-descricao">Soma dos agendamentos de cada data, com os serviços adicionais.</p>
            {dias.length === 0 ? (
              <p className="lista-vazia">Nenhum agendamento feito ainda.</p>
            ) : (
              <>
                <GraficoColunas itens={dias} formatarValor={formatarMoeda} formatarEixo={formatarMoedaCurta} />
                <details className="ver-tabela">
                  <summary>Ver como tabela</summary>
                  <table className="tabela">
                    <thead>
                      <tr>
                        <th scope="col">Data</th>
                        <th scope="col" className="col-numero">Faturamento</th>
                      </tr>
                    </thead>
                    <tbody>
                      {dias.map(d => (
                        <tr key={d.chave}>
                          <td className="numero">{d.rotuloLongo}</td>
                          <td className="col-numero numero">{formatarMoeda(d.valor)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </details>
              </>
            )}
          </section>

          <section className="relatorio" aria-labelledby="rel-ocupacao">
            <h2 id="rel-ocupacao">Ocupação dos ambientes</h2>
            <p className="relatorio-descricao">
              Horas cobradas em cada ambiente. Ambientes sem reserva aparecem com zero.
            </p>
            <GraficoBarras
              formatarValor={horas}
              itens={ocupacaoOrdenada.map(o => ({
                chave: o.ambienteId,
                rotulo: o.nome,
                detalhe: `${plural(o.quantidadeAgendamentos, 'agendamento', 'agendamentos')}, ${formatarMoeda(o.valorTotal)}`,
                valor: o.horasCobradas,
                tipo: o.codigo,
              }))}
            />
          </section>

          <section className="relatorio" aria-labelledby="rel-servicos">
            <h2 id="rel-servicos">Serviços vendidos</h2>
            <p className="relatorio-descricao">Valor arrecadado com cada serviço adicional.</p>
            {servicos.length === 0 ? (
              <p className="lista-vazia">Nenhum serviço adicional vendido ainda.</p>
            ) : (
              <GraficoBarras
                formatarValor={formatarMoeda}
                itens={servicosOrdenados.map(s => ({
                  chave: s.servico,
                  rotulo: s.servico,
                  detalhe: plural(s.quantidade, 'venda', 'vendas'),
                  valor: s.valorTotal,
                }))}
              />
            )}
          </section>
        </div>
      )}
    </>
  )
}

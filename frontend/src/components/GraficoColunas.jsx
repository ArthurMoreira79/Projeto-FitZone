import './GraficoColunas.css'

// topo "redondo" do eixo: 315 → 500, 1.240 → 2.000. Marcas limpas em vez de 315 / 157,50 / 0
function topoDoEixo(maximo) {
  if (maximo <= 0) return 1
  const escala = 10 ** Math.floor(Math.log10(maximo))
  return [1, 2, 2.5, 5, 10].map(m => m * escala).find(v => v >= maximo)
}

// Colunas de uma série só (uma cor). itens: [{ chave, rotulo, rotuloLongo, valor }]
// Só a maior coluna leva o valor escrito; as outras mostram ao passar o mouse ou focar (Tab),
// e a tabela abaixo do gráfico tem todos os valores.
export default function GraficoColunas({ itens, formatarValor, formatarEixo = formatarValor }) {
  const topo = topoDoEixo(Math.max(0, ...itens.map(i => i.valor)))
  const maior = itens.reduce((atual, i) => (!atual || i.valor > atual.valor ? i : atual), null)
  const marcas = [topo, topo / 2, 0]

  return (
    <div className="grafico-colunas">
      <div className="colunas-eixo numero" aria-hidden="true">
        {marcas.map(m => (
          <span key={m}>{formatarEixo(m)}</span>
        ))}
      </div>

      <div className="colunas-rolagem">
        <div className="colunas-plot">
          <div className="colunas-grade" aria-hidden="true">
            {marcas.map(m => <span key={m} />)}
          </div>

          <ol className="colunas-lista">
            {itens.map((item, indice) => {
              const altura = (item.valor / topo) * 100
              const lado = indice === 0 ? ' dica-inicio' : indice === itens.length - 1 ? ' dica-fim' : ''
              return (
                <li key={item.chave} className="coluna">
                  {/* o alvo do mouse/foco é a faixa inteira da coluna, não só a barra pintada */}
                  <span
                    className="coluna-alvo"
                    tabIndex={0}
                    role="img"
                    aria-label={`${item.rotuloLongo}: ${formatarValor(item.valor)}`}
                  >
                    <span className="coluna-barra" style={{ height: `${altura}%` }} />
                    {item === maior && item.valor > 0 && (
                      <span className="coluna-rotulo numero" style={{ bottom: `calc(${altura}% + 6px)` }}>
                        {formatarValor(item.valor)}
                      </span>
                    )}
                    <span
                      className={`coluna-dica${lado}`}
                      style={{ bottom: `min(calc(${altura}% + 8px), calc(100% - 52px))` }}
                      aria-hidden="true"
                    >
                      <strong className="numero">{formatarValor(item.valor)}</strong>
                      <span>{item.rotuloLongo}</span>
                    </span>
                  </span>
                </li>
              )
            })}
          </ol>
        </div>

        <ol className="colunas-x numero" aria-hidden="true">
          {itens.map(item => (
            <li key={item.chave}>{item.rotulo}</li>
          ))}
        </ol>
      </div>
    </div>
  )
}

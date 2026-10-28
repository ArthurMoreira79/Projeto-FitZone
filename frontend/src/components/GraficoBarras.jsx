import './GraficoBarras.css'

// Barras horizontais de uma série só (uma cor), com o valor na ponta.
// itens: [{ chave, rotulo, detalhe?, valor, tipo? }]
// "tipo" (código do ambiente) mostra a anilha ao lado do nome: a cor identifica o tipo,
// mas quem carrega a identidade é o nome escrito (as cores das anilhas não passam no teste de daltonismo).
export default function GraficoBarras({ itens, formatarValor }) {
  const maximo = Math.max(0, ...itens.map(i => i.valor)) || 1

  return (
    <ul className="grafico-barras">
      {itens.map(item => (
        <li key={item.chave} className="barra-linha" data-tipo={item.tipo}>
          <span className="barra-nome">
            {item.tipo && <span className="anilha" aria-hidden="true" />}
            <span>
              <span className="barra-rotulo">{item.rotulo}</span>
              {item.detalhe && <span className="barra-detalhe numero">{item.detalhe}</span>}
            </span>
          </span>
          <span className="barra-trilho">
            {/* a maior barra ocupa o trilho menos o espaço do valor: o texto nunca é empurrado para fora */}
            <span className="barra" style={{ width: `calc((100% - 7rem) * ${item.valor / maximo})` }} />
            <span className="barra-valor numero">{formatarValor(item.valor)}</span>
          </span>
        </li>
      ))}
    </ul>
  )
}

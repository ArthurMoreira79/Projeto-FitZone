import './CabecalhoPagina.css'

// children = botões de ação, opcionais (ex.: "Novo aluno")
export default function CabecalhoPagina({ titulo, descricao, children }) {
  return (
    <header className="cabecalho-pagina">
      <div>
        <h1>{titulo}</h1>
        {descricao && <p className="cabecalho-descricao">{descricao}</p>}
      </div>
      {children && <div className="cabecalho-acoes">{children}</div>}
    </header>
  )
}
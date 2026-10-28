const LIMITE_OBSERVACOES = 200 // o mesmo @Size(max = 200) do AmbienteRequest e do AmbienteObservacoesRequest

// usado no cadastro e na edição de ambiente: mesmo campo, mesmo limite, mesmo contador
export default function CampoObservacoes({ id, valor, onChange, erro }) {
  return (
    <div className="campo">
      <label htmlFor={id}>
        Observações <span className="campo-opcional">(opcional)</span>
      </label>
      <textarea
        id={id}
        value={valor}
        onChange={e => onChange(e.target.value)}
        maxLength={LIMITE_OBSERVACOES}
        placeholder="Ex.: térreo, climatizada, com espelhos"
        aria-invalid={Boolean(erro)}
        aria-describedby={`${id}-rodape`}
      />
      <div id={`${id}-rodape`} className="campo-rodape">
        {erro && <p className="campo-erro">{erro}</p>}
        <span className="campo-contador numero">{valor.length}/{LIMITE_OBSERVACOES}</span>
      </div>
    </div>
  )
}

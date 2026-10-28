// rótulo + input + mensagem de erro ligados entre si (htmlFor, aria-invalid, aria-describedby).
// Props extras (type, maxLength, inputMode, placeholder...) vão direto para o <input>.
export default function CampoTexto({ id, rotulo, valor, onChange, erro, ...props }) {
  return (
    <div className="campo">
      <label htmlFor={id}>{rotulo}</label>
      <input
        id={id}
        value={valor}
        onChange={e => onChange(e.target.value)}
        aria-invalid={Boolean(erro)}
        aria-describedby={erro ? `${id}-erro` : undefined}
        {...props}
      />
      {erro && <p id={`${id}-erro`} className="campo-erro">{erro}</p>}
    </div>
  )
}

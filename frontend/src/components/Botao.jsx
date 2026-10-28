import './Botao.css'

// variante: 'principal' | 'secundario' | 'perigo' | 'discreto' | 'discreto-perigo'
// tamanho: 'normal' | 'pequeno' (o pequeno é para ações dentro de tabelas)
export default function Botao({ variante = 'principal', tamanho = 'normal', type = 'button', className = '', ...props }) {
  return <button type={type} className={`botao botao-${variante} botao-${tamanho} ${className}`} {...props} />
}

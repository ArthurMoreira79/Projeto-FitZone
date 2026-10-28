import './Botao.css'

// variante: 'principal' | 'secundario' | 'perigo'
export default function Botao({ variante = 'principal', type = 'button', className = '', ...props }) {
  return <button type={type} className={`botao botao-${variante} ${className}`} {...props} />
}
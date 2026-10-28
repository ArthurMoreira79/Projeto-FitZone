import Botao from './Botao'
import './MensagemErro.css'

export default function MensagemErro({ erro, onTentarDeNovo }) {
  return (
    <div className="mensagem-erro" role="alert">
      <p>{erro.message}</p>
      {onTentarDeNovo && (
        <Botao variante="secundario" onClick={onTentarDeNovo}>Tentar de novo</Botao>
      )}
    </div>
  )
}
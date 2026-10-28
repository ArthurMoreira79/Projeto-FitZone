import { useId } from 'react'
import Botao from './Botao'
import Modal from './Modal'
import './ModalConfirmacao.css'

export default function ModalConfirmacao({
  aberto,
  titulo,
  mensagem,
  rotuloConfirmar,
  onConfirmar,
  onCancelar,
  processando = false,
}) {
  const tituloId = useId()

  return (
    <Modal aberto={aberto} onFechar={onCancelar} bloqueado={processando} tituloId={tituloId} className="modal-perigo">
      <h2 id={tituloId}>{titulo}</h2>
      <p className="modal-mensagem">{mensagem}</p>

      <div className="modal-acoes">
        {/* "Voltar" vem primeiro: showModal() foca o primeiro botão, então Enter sem querer não exclui nada */}
        <Botao variante="secundario" onClick={onCancelar} disabled={processando}>
          Voltar
        </Botao>
        <Botao variante="perigo" onClick={onConfirmar} disabled={processando}>
          {rotuloConfirmar}
        </Botao>
      </div>
    </Modal>
  )
}

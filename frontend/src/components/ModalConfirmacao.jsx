import { useEffect, useId, useRef } from 'react'
import Botao from './Botao'
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
  const ref = useRef(null)
  const tituloId = useId()

  // o estado "aberto" do React manda; o efeito só sincroniza o <dialog> com ele
  useEffect(() => {
    const dialog = ref.current
    if (aberto && !dialog.open) dialog.showModal()
    if (!aberto && dialog.open) dialog.close()
  }, [aberto])

  function aoApertarEsc(evento) {
    evento.preventDefault() // impede o navegador de fechar sozinho...
    if (!processando) onCancelar() // ...e deixa o React decidir
  }

  return (
    <dialog ref={ref} className="modal" aria-labelledby={tituloId} onCancel={aoApertarEsc}>
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
    </dialog>
  )
}
import { useEffect, useRef } from 'react'
import './Modal.css'

// <dialog> nativo controlado pelo React: a prop "aberto" manda, o efeito sincroniza
export default function Modal({ aberto = true, onFechar, bloqueado = false, tituloId, className = '', children }) {
  const ref = useRef(null)

  useEffect(() => {
    const dialog = ref.current
    if (aberto && !dialog.open) dialog.showModal()
    if (!aberto && dialog.open) dialog.close()
  }, [aberto])

  function aoApertarEsc(evento) {
    evento.preventDefault()
    // "bloqueado" = requisição em andamento: Esc não fecha no meio do envio
    if (!bloqueado) onFechar()
  }

  return (
    <dialog ref={ref} className={`modal ${className}`} aria-labelledby={tituloId} onCancel={aoApertarEsc}>
      {children}
    </dialog>
  )
}

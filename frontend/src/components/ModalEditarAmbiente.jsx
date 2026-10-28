import { useId, useState } from 'react'
import { ambientesApi } from '../api/ambientes'
import Botao from './Botao'
import CampoObservacoes from './CampoObservacoes'
import MensagemErro from './MensagemErro'
import Modal from './Modal'

export default function ModalEditarAmbiente({ ambiente, onFechar, onSalvo }) {
  const id = useId()
  // o banco guarda null quando não há observação; o textarea precisa de string
  const original = ambiente.observacoes ?? ''
  const [observacoes, setObservacoes] = useState(original)
  const [erroCampo, setErroCampo] = useState(null)
  const [erroGeral, setErroGeral] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const alterado = observacoes.trim() !== original.trim()

  async function enviar(evento) {
    evento.preventDefault()
    setEnviando(true)
    setErroCampo(null)
    setErroGeral(null)

    try {
      const atualizado = await ambientesApi.atualizarObservacoes(ambiente.id, observacoes.trim() || null)
      onSalvo(atualizado)
    } catch (erro) {
      const campo = erro.campos.find(c => c.nome === 'observacoes')
      if (campo) setErroCampo(campo.mensagem)
      else setErroGeral(erro)
      setEnviando(false)
    }
  }

  return (
    <Modal onFechar={onFechar} bloqueado={enviando} tituloId={`${id}-titulo`}>
      <h2 id={`${id}-titulo`}>{ambiente.nome}</h2>
      <p className="modal-mensagem" style={{ marginBottom: 20 }}>
        Só as observações podem ser alteradas. Nome e valor por hora vêm do tipo do ambiente.
      </p>

      <form onSubmit={enviar} noValidate>
        {erroGeral && (
          <div style={{ marginBottom: 20 }}>
            <MensagemErro erro={erroGeral} />
          </div>
        )}

        <CampoObservacoes id={`${id}-obs`} valor={observacoes} onChange={setObservacoes} erro={erroCampo} />

        <div className="modal-acoes">
          <Botao variante="secundario" onClick={onFechar} disabled={enviando}>Cancelar</Botao>
          {/* sem mudança não há o que salvar: evita uma requisição à toa */}
          <Botao type="submit" disabled={enviando || !alterado}>
            {enviando ? 'Salvando…' : 'Salvar observações'}
          </Botao>
        </div>
      </form>
    </Modal>
  )
}

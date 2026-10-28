import { useId, useState } from 'react'
import { ambientesApi } from '../api/ambientes'
import { formatarMoeda } from '../formatos'
import Botao from './Botao'
import CampoObservacoes from './CampoObservacoes'
import MensagemErro from './MensagemErro'
import Modal from './Modal'

export default function ModalNovoAmbiente({ grupos, onFechar, onCadastrado }) {
  const id = useId()
  const [tipo, setTipo] = useState('')
  const [observacoes, setObservacoes] = useState('')
  const [errosCampos, setErrosCampos] = useState({})
  const [erroGeral, setErroGeral] = useState(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento) {
    evento.preventDefault() // sem isso o navegador recarregaria a página ao enviar o <form>

    if (!tipo) {
      setErrosCampos({ tipo: 'Escolha o tipo do ambiente.' })
      return
    }

    setEnviando(true)
    setErrosCampos({})
    setErroGeral(null)

    try {
      const criado = await ambientesApi.cadastrar(tipo, observacoes.trim() || null)
      onCadastrado(criado)
    } catch (erro) {
      if (erro.campos.length > 0) {
        // 400 de validação: [{ nome, mensagem }] vira { tipo: '...', observacoes: '...' }
        setErrosCampos(Object.fromEntries(erro.campos.map(c => [c.nome, c.mensagem])))
      } else {
        // 409 (faixa lotada), servidor fora do ar etc.: mensagem geral no topo do formulário
        setErroGeral(erro)
      }
      setEnviando(false)
    }
  }

  return (
    <Modal onFechar={onFechar} bloqueado={enviando} tituloId={`${id}-titulo`}>
      <h2 id={`${id}-titulo`}>Novo ambiente</h2>

      {/* noValidate: as mensagens são nossas, não os balões padrão do navegador */}
      <form onSubmit={enviar} noValidate>
        {erroGeral && (
          <div style={{ marginBottom: 20 }}>
            <MensagemErro erro={erroGeral} />
          </div>
        )}

        <div className="campo">
          <label htmlFor={`${id}-tipo`}>Tipo</label>
          <select
            id={`${id}-tipo`}
            value={tipo}
            onChange={e => setTipo(e.target.value)}
            aria-invalid={Boolean(errosCampos.tipo)}
            aria-describedby={errosCampos.tipo ? `${id}-tipo-erro` : undefined}
          >
            <option value="" disabled>Escolha o tipo</option>
            {grupos.map(g => (
              <option key={g.tipo.codigo} value={g.tipo.codigo} disabled={g.lotado}>
                {`${g.tipo.nome}, ${formatarMoeda(g.tipo.valorHora)} por hora${g.lotado ? ' (sem vagas)' : ''}`}
              </option>
            ))}
          </select>
          {errosCampos.tipo && (
            <p id={`${id}-tipo-erro`} className="campo-erro">{errosCampos.tipo}</p>
          )}
        </div>

        <CampoObservacoes
          id={`${id}-obs`}
          valor={observacoes}
          onChange={setObservacoes}
          erro={errosCampos.observacoes}
        />

        <div className="modal-acoes">
          <Botao variante="secundario" onClick={onFechar} disabled={enviando}>Cancelar</Botao>
          <Botao type="submit" disabled={enviando}>
            {enviando ? 'Cadastrando…' : 'Cadastrar ambiente'}
          </Botao>
        </div>
      </form>
    </Modal>
  )
}

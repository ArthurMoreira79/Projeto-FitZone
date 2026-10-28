import { useState } from 'react'
import CabecalhoPagina from '../components/CabecalhoPagina'
import Botao from '../components/Botao'
import ModalConfirmacao from '../components/ModalConfirmacao'
import { useAviso } from '../avisos/AvisoContext'

export default function Painel() {
  const avisar = useAviso()
  const [modalAberto, setModalAberto] = useState(false)

  return (
    <>
      <CabecalhoPagina
        titulo="Painel"
        descricao="Resumo de agendamentos, ocupação dos ambientes e faturamento."
      />

      {/* TESTE TEMPORÁRIO dos componentes base */}
      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
        <Botao onClick={() => avisar('Ambiente cadastrado.')}>Aviso de sucesso</Botao>
        <Botao variante="secundario" onClick={() => avisar('Sala de Yoga 201 tem agendamentos e não pode ser excluída.', 'erro')}>
          Aviso de erro
        </Botao>
        <Botao variante="perigo" onClick={() => setModalAberto(true)}>Excluir ambiente</Botao>
        <Botao disabled>Desativado</Botao>
      </div>

      <ModalConfirmacao
        aberto={modalAberto}
        titulo="Excluir Sala de Yoga 201?"
        mensagem="O ambiente sai da lista de reservas. Esta ação não pode ser desfeita."
        rotuloConfirmar="Excluir ambiente"
        onConfirmar={() => {
          setModalAberto(false)
          avisar('Ambiente excluído.')
        }}
        onCancelar={() => setModalAberto(false)}
      />
    </>
  )
}
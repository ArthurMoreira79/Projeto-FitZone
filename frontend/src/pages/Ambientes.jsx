import { useEffect, useState } from 'react'
import { ambientesApi } from '../api/ambientes'
import CabecalhoPagina from '../components/CabecalhoPagina'
import GrupoAmbientes from '../components/GrupoAmbientes'
import MensagemErro from '../components/MensagemErro'
import Botao from '../components/Botao'
import ModalNovoAmbiente from '../components/ModalNovoAmbiente'
import ModalEditarAmbiente from '../components/ModalEditarAmbiente'
import ModalConfirmacao from '../components/ModalConfirmacao'
import { useAviso } from '../avisos/AvisoContext'
import './Ambientes.css'

export default function Ambientes() {
  const [tipos, setTipos] = useState([])
  const [ambientes, setAmbientes] = useState([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(null)
  // mudar a versão faz o useEffect rodar de novo: é assim que a tela recarrega
  const [versao, setVersao] = useState(0)
  const [novoAberto, setNovoAberto] = useState(false)
  // guardam o ambiente da linha clicada; null = modal fechado
  const [emEdicao, setEmEdicao] = useState(null)
  const [paraExcluir, setParaExcluir] = useState(null)
  const [excluindo, setExcluindo] = useState(false)
  const avisar = useAviso()

  useEffect(() => {
    let ignorar = false

    async function carregar() {
      setCarregando(true)
      setErro(null)
      try {
        // as duas requisições saem juntas; espera as duas terminarem
        const [listaTipos, listaAmbientes] = await Promise.all([
          ambientesApi.listarTipos(),
          ambientesApi.listar(),
        ])
        if (ignorar) return
        setTipos(listaTipos)
        setAmbientes(listaAmbientes)
      } catch (e) {
        if (!ignorar) setErro(e)
      } finally {
        if (!ignorar) setCarregando(false)
      }
    }

    carregar()
    // limpeza: se a pessoa sair da tela antes da resposta, a resposta velha é descartada
    return () => {
      ignorar = true
    }
  }, [versao])

  const recarregar = () => setVersao(v => v + 1)

  // um grupo por tipo, na ordem do enum; a capacidade sai da faixa de ids (201–210 = 10)
  const grupos = tipos.map(tipo => {
    const salas = ambientes.filter(a => a.codigo === tipo.codigo)
    const capacidade = tipo.fimFaixa - tipo.inicioFaixa + 1
    return { tipo, salas, capacidade, lotado: salas.length >= capacidade }
  })
  const todosLotados = grupos.length > 0 && grupos.every(g => g.lotado)

  function aoCadastrar(criado) {
    setNovoAberto(false)
    avisar(`Ambiente cadastrado: ${criado.nome}.`)
    recarregar()
  }

  function aoSalvarObservacoes(atualizado) {
    setEmEdicao(null)
    avisar(`Observações salvas: ${atualizado.nome}.`)
    recarregar()
  }

  async function confirmarExclusao() {
    const alvo = paraExcluir
    setExcluindo(true)
    try {
      await ambientesApi.excluir(alvo.id)
      avisar(`Ambiente excluído: ${alvo.nome}.`)
      recarregar()
    } catch (e) {
      // a mensagem do 409 no back é genérica (vale para qualquer FK); aqui a tela sabe o contexto
      avisar(
        e.status === 409
          ? `Não é possível excluir ${alvo.nome}: existem agendamentos para este ambiente.`
          : e.message,
        'erro',
      )
    } finally {
      // fecha o modal junto com o aviso: o aviso nunca fica escondido atrás do fundo escuro
      setExcluindo(false)
      setParaExcluir(null)
    }
  }

  return (
    <>
      <CabecalhoPagina
        titulo="Ambientes"
        descricao="Salas e piscina disponíveis para reserva, com o valor cobrado por hora."
      >
        <Botao onClick={() => setNovoAberto(true)} disabled={tipos.length === 0 || todosLotados}>
          Novo ambiente
        </Botao>
      </CabecalhoPagina>

      {/* "carregando" só na primeira vez: ao recarregar, a lista antiga fica na tela até a nova chegar */}
      {carregando && tipos.length === 0 && <p className="estado-carregando">Carregando ambientes…</p>}

      {erro && <MensagemErro erro={erro} onTentarDeNovo={recarregar} />}

      {!erro && tipos.length > 0 && (
        <div className="grupos-ambientes">
          {grupos.map(g => (
            <GrupoAmbientes
              key={g.tipo.codigo}
              tipo={g.tipo}
              salas={g.salas}
              capacidade={g.capacidade}
              onEditar={setEmEdicao}
              onExcluir={setParaExcluir}
            />
          ))}
        </div>
      )}

      {novoAberto && (
        <ModalNovoAmbiente
          grupos={grupos}
          onFechar={() => setNovoAberto(false)}
          onCadastrado={aoCadastrar}
        />
      )}

      {emEdicao && (
        <ModalEditarAmbiente
          ambiente={emEdicao}
          onFechar={() => setEmEdicao(null)}
          onSalvo={aoSalvarObservacoes}
        />
      )}

      {paraExcluir && (
        <ModalConfirmacao
          aberto
          titulo={`Excluir ${paraExcluir.nome}?`}
          mensagem="O ambiente sai da lista de reservas e o id fica livre para um novo cadastro. Ambientes com agendamentos não podem ser excluídos."
          rotuloConfirmar="Excluir ambiente"
          onConfirmar={confirmarExclusao}
          onCancelar={() => setParaExcluir(null)}
          processando={excluindo}
        />
      )}
    </>
  )
}

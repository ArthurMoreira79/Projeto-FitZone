import { useId, useState } from 'react'
import { agendamentosApi } from '../api/agendamentos'
import { servicosApi } from '../api/servicos'
import { formatarDataLonga, formatarHora, formatarMoeda, plural } from '../formatos'
import { useDados } from '../hooks/useDados'
import Botao from './Botao'
import MensagemErro from './MensagemErro'
import Modal from './Modal'
import ModalConfirmacao from './ModalConfirmacao'
import './ModalDetalheAgendamento.css'

// como o preço vira valor: vem do campo "cobranca" do catálogo, não de uma lista de códigos no front
function descreverPreco(tipo) {
  const preco = formatarMoeda(tipo.valor)
  if (tipo.cobranca === 'POR_HORA') return `${preco} por hora cobrada`
  if (tipo.cobranca === 'POR_UNIDADE') return `${preco} por unidade`
  return preco
}

export default function ModalDetalheAgendamento({ agendamento, onFechar, onAlterado, onCancelado }) {
  const id = useId()
  // começa com o que a lista já tinha; cada serviço adicionado troca pelo agendamento que a API devolve
  const [ag, setAg] = useState(agendamento)
  const tipos = useDados(servicosApi.listarTipos)

  const [tipoServico, setTipoServico] = useState('')
  const [quantidade, setQuantidade] = useState('1')
  const [erroServico, setErroServico] = useState(null)
  const [adicionado, setAdicionado] = useState(null)
  const [adicionando, setAdicionando] = useState(false)

  const [confirmandoCancelamento, setConfirmandoCancelamento] = useState(false)
  const [cancelando, setCancelando] = useState(false)
  const [erroCancelamento, setErroCancelamento] = useState(null)

  const tipoEscolhido = tipos.dados?.find(t => t.codigo === tipoServico)
  // só o que é cobrado por unidade pede quantidade (hoje, o locker)
  const porUnidade = tipoEscolhido?.cobranca === 'POR_UNIDADE'

  // a parte do ambiente sai por diferença: total da API menos os serviços (nada de recalcular horas aqui)
  const totalServicos = ag.servicos.reduce((soma, s) => soma + s.valorTotal, 0)
  const valorAmbiente = ag.valorTotal - totalServicos
  const horario = `das ${formatarHora(ag.horaInicio)} às ${formatarHora(ag.horaFim)}`

  async function adicionar(evento) {
    evento.preventDefault()
    setAdicionado(null)

    if (!tipoEscolhido) {
      setErroServico('Escolha o serviço.')
      return
    }
    const qtd = Number(quantidade)
    if (porUnidade && !(Number.isInteger(qtd) && qtd >= 1)) {
      setErroServico('Informe a quantidade: um número inteiro, 1 ou mais.')
      return
    }

    setAdicionando(true)
    setErroServico(null)
    try {
      const atualizado = await agendamentosApi.adicionarServico(ag.id, {
        tipo: tipoServico,
        quantidade: porUnidade ? qtd : null,
      })
      // o serviço novo é o id que não existia antes
      const novo = atualizado.servicos.find(s => !ag.servicos.some(antigo => antigo.id === s.id))
      setAg(atualizado)
      setAdicionado(novo?.descricao ?? tipoEscolhido.nome)
      setTipoServico('')
      setQuantidade('1')
      onAlterado() // a lista por trás atualiza o total
    } catch (erro) {
      setErroServico(erro.campos[0]?.mensagem ?? erro.message)
    } finally {
      setAdicionando(false)
    }
  }

  async function cancelar() {
    setCancelando(true)
    try {
      await agendamentosApi.cancelar(ag.id)
      onCancelado(ag)
    } catch (erro) {
      setErroCancelamento(erro)
      setCancelando(false)
      setConfirmandoCancelamento(false)
    }
  }

  return (
    <>
      <Modal
        onFechar={onFechar}
        bloqueado={adicionando || cancelando}
        tituloId={`${id}-titulo`}
        className="modal-largo detalhe-agendamento"
      >
        <header className="detalhe-cabecalho" data-tipo={ag.ambienteCodigo}>
          <span className="anilha" aria-hidden="true" />
          <div>
            <h2 id={`${id}-titulo`}>{ag.ambienteNome}</h2>
            <p className="detalhe-quando">
              {formatarDataLonga(ag.data)}, {horario}
            </p>
            <p className="detalhe-aluno">{ag.alunoNome}</p>
          </div>
        </header>

        {erroCancelamento && (
          <div style={{ marginBottom: 20 }}>
            <MensagemErro erro={erroCancelamento} />
          </div>
        )}

        <section aria-labelledby={`${id}-valor`}>
          <h3 id={`${id}-valor`}>Valor</h3>
          <ul className="composicao numero">
            <li>
              <span>{`${ag.ambienteNome} (${plural(ag.horasCobradas, 'hora cobrada', 'horas cobradas')})`}</span>
              <span>{formatarMoeda(valorAmbiente)}</span>
            </li>
            {ag.servicos.map(s => (
              <li key={s.id}>
                <span>{s.descricao}</span>
                <span>{formatarMoeda(s.valorTotal)}</span>
              </li>
            ))}
            <li className="composicao-total">
              <span>Total</span>
              <span>{formatarMoeda(ag.valorTotal)}</span>
            </li>
          </ul>
        </section>

        <section aria-labelledby={`${id}-servico`} className="detalhe-servico">
          <h3 id={`${id}-servico`}>Adicionar serviço</h3>

          {tipos.erro && <MensagemErro erro={tipos.erro} onTentarDeNovo={tipos.recarregar} />}

          {tipos.dados && (
            <form onSubmit={adicionar} noValidate className="form-servico">
              <div className="campo">
                <label htmlFor={`${id}-tipo`}>Serviço</label>
                <select
                  id={`${id}-tipo`}
                  value={tipoServico}
                  onChange={e => {
                    setTipoServico(e.target.value)
                    setErroServico(null)
                  }}
                  aria-invalid={Boolean(erroServico)}
                  aria-describedby={`${id}-servico-status`}
                >
                  <option value="" disabled>Escolha o serviço</option>
                  {tipos.dados.map(t => (
                    <option key={t.codigo} value={t.codigo}>{`${t.nome}, ${descreverPreco(t)}`}</option>
                  ))}
                </select>
              </div>

              {porUnidade && (
                <div className="campo campo-quantidade">
                  <label htmlFor={`${id}-qtd`}>Quantidade</label>
                  <input
                    id={`${id}-qtd`}
                    type="number"
                    inputMode="numeric"
                    min={1}
                    step={1}
                    value={quantidade}
                    onChange={e => {
                      setQuantidade(e.target.value)
                      setErroServico(null)
                    }}
                  />
                </div>
              )}

              <Botao type="submit" variante="secundario" disabled={adicionando}>
                {adicionando ? 'Adicionando…' : 'Adicionar serviço'}
              </Botao>
            </form>
          )}

          {/* resultado dentro do modal (e não no aviso do canto, que ficaria atrás do fundo escuro) */}
          <div id={`${id}-servico-status`} role="status" className="servico-status">
            {erroServico && <p className="campo-erro">{erroServico}</p>}
            {adicionado && <p className="servico-adicionado">Serviço adicionado: {adicionado}.</p>}
          </div>
        </section>

        <div className="modal-acoes detalhe-acoes">
          <Botao
            variante="discreto-perigo"
            onClick={() => setConfirmandoCancelamento(true)}
            disabled={adicionando || cancelando}
          >
            Cancelar agendamento
          </Botao>
          <Botao variante="secundario" onClick={onFechar} disabled={adicionando || cancelando}>
            Fechar
          </Botao>
        </div>
      </Modal>

      {/* irmão do modal de detalhe, não filho: os dois <dialog> ficam empilhados na camada do topo */}
      {confirmandoCancelamento && (
        <ModalConfirmacao
          aberto
          titulo="Cancelar este agendamento?"
          mensagem={`O horário ${horario} em ${ag.ambienteNome} fica livre${
            ag.servicos.length > 0 ? ' e os serviços adicionados são removidos' : ''
          }. Esta ação não pode ser desfeita.`}
          rotuloConfirmar="Cancelar agendamento"
          onConfirmar={cancelar}
          onCancelar={() => setConfirmandoCancelamento(false)}
          processando={cancelando}
        />
      )}
    </>
  )
}

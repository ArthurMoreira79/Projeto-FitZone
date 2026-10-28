import { useState } from 'react'
import { alunosApi } from '../api/alunos'
import { useAviso } from '../avisos/AvisoContext'
import Botao from '../components/Botao'
import CabecalhoPagina from '../components/CabecalhoPagina'
import MensagemErro from '../components/MensagemErro'
import ModalAluno from '../components/ModalAluno'
import ModalConfirmacao from '../components/ModalConfirmacao'
import { formatarCpf, formatarData, formatarTelefone, normalizar, plural, somenteDigitos } from '../formatos'
import { useDados } from '../hooks/useDados'
import './Alunos.css'

export default function Alunos() {
  const { dados, carregando, erro, recarregar } = useDados(alunosApi.listar)
  const [busca, setBusca] = useState('')
  const [novoAberto, setNovoAberto] = useState(false)
  const [emEdicao, setEmEdicao] = useState(null)
  const [paraExcluir, setParaExcluir] = useState(null)
  const [excluindo, setExcluindo] = useState(false)
  const avisar = useAviso()

  const alunos = dados ?? []
  // [...alunos] copia antes de ordenar: sort() altera o array original, e o original é estado do React
  const ordenados = [...alunos].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))

  // filtro no navegador: a lista inteira já está carregada, então filtrar é instantâneo
  const termo = normalizar(busca.trim())
  const digitosBusca = somenteDigitos(busca)
  const filtrados = termo === ''
    ? ordenados
    : ordenados.filter(a =>
        normalizar(a.nome).includes(termo) ||
        normalizar(a.email).includes(termo) ||
        // CPF só a partir de 3 dígitos: com 1 ou 2, quase todo CPF "combinaria"
        (digitosBusca.length >= 3 && a.cpf.includes(digitosBusca)))

  function fecharFormulario() {
    setNovoAberto(false)
    setEmEdicao(null)
  }

  function aoSalvar(salvo, eraNovo) {
    fecharFormulario()
    avisar(eraNovo ? `Aluno cadastrado: ${salvo.nome}.` : `Dados salvos: ${salvo.nome}.`)
    recarregar()
  }

  async function confirmarExclusao() {
    const alvo = paraExcluir
    setExcluindo(true)
    try {
      await alunosApi.excluir(alvo.id)
      avisar(`Aluno excluído: ${alvo.nome}.`)
      recarregar()
    } catch (e) {
      avisar(
        e.status === 409 ? `Não é possível excluir ${alvo.nome}: existem agendamentos para este aluno.` : e.message,
        'erro',
      )
    } finally {
      setExcluindo(false)
      setParaExcluir(null)
    }
  }

  const contagem = termo === ''
    ? `${plural(alunos.length, 'aluno cadastrado', 'alunos cadastrados')}`
    : `${filtrados.length} de ${plural(alunos.length, 'aluno', 'alunos')}`

  return (
    <>
      <CabecalhoPagina titulo="Alunos" descricao="Cadastre alunos e encontre o cadastro pelo nome, CPF ou e-mail.">
        <Botao onClick={() => setNovoAberto(true)} disabled={!dados}>Novo aluno</Botao>
      </CabecalhoPagina>

      {carregando && !dados && <p className="estado-carregando">Carregando alunos…</p>}

      {erro && <MensagemErro erro={erro} onTentarDeNovo={recarregar} />}

      {!erro && dados && (
        <>
          <div className="barra-busca">
            <div className="campo">
              <label htmlFor="busca-aluno">Buscar aluno</label>
              <input
                id="busca-aluno"
                type="search"
                value={busca}
                onChange={e => setBusca(e.target.value)}
                placeholder="Nome, CPF ou e-mail"
                autoComplete="off"
              />
            </div>
            {/* aria-live: o leitor de tela anuncia quantos ficaram a cada letra digitada */}
            <p className="contagem numero" aria-live="polite">{contagem}</p>
          </div>

          {alunos.length === 0 && (
            <div className="lista-vazia">
              <p>Nenhum aluno cadastrado ainda.</p>
              <Botao variante="secundario" onClick={() => setNovoAberto(true)}>Cadastrar o primeiro aluno</Botao>
            </div>
          )}

          {alunos.length > 0 && filtrados.length === 0 && (
            <div className="lista-vazia">
              <p>Nenhum aluno encontrado para “{busca.trim()}”.</p>
              <Botao variante="secundario" onClick={() => setBusca('')}>Limpar busca</Botao>
            </div>
          )}

          {filtrados.length > 0 && (
            <div className="tabela-rolagem">
              <table className="tabela tabela-alunos">
                {/* larguras fixas: as colunas não "pulam" enquanto a busca filtra as linhas */}
                <colgroup>
                  <col />
                  <col className="col-cpf" />
                  <col />
                  <col className="col-telefone" />
                  <col className="col-cadastro" />
                  <col className="col-acoes" />
                </colgroup>
                <thead>
                  <tr>
                    <th scope="col">Nome</th>
                    <th scope="col">CPF</th>
                    <th scope="col">E-mail</th>
                    <th scope="col">Telefone</th>
                    <th scope="col">Cadastro</th>
                    <th scope="col"><span className="visualmente-oculto">Ações</span></th>
                  </tr>
                </thead>
                <tbody>
                  {filtrados.map(aluno => (
                    <tr key={aluno.id}>
                      <td>{aluno.nome}</td>
                      <td className="numero">{formatarCpf(aluno.cpf)}</td>
                      <td className="celula-email">{aluno.email}</td>
                      <td className="numero">{formatarTelefone(aluno.telefone)}</td>
                      <td className="numero">{formatarData(aluno.dataCadastro)}</td>
                      <td>
                        <div className="acoes-linha">
                          <Botao
                            variante="discreto"
                            tamanho="pequeno"
                            onClick={() => setEmEdicao(aluno)}
                            aria-label={`Editar ${aluno.nome}`}
                          >
                            Editar
                          </Botao>
                          <Botao
                            variante="discreto-perigo"
                            tamanho="pequeno"
                            onClick={() => setParaExcluir(aluno)}
                            aria-label={`Excluir ${aluno.nome}`}
                          >
                            Excluir
                          </Botao>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      {(novoAberto || emEdicao) && (
        <ModalAluno aluno={emEdicao} onFechar={fecharFormulario} onSalvo={aoSalvar} />
      )}

      {paraExcluir && (
        <ModalConfirmacao
          aberto
          titulo={`Excluir ${paraExcluir.nome}?`}
          mensagem="O cadastro do aluno é apagado. Alunos com agendamentos não podem ser excluídos: cancele os agendamentos antes."
          rotuloConfirmar="Excluir aluno"
          onConfirmar={confirmarExclusao}
          onCancelar={() => setParaExcluir(null)}
          processando={excluindo}
        />
      )}
    </>
  )
}

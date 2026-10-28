import { useId, useState } from 'react'
import { alunosApi } from '../api/alunos'
import { formatarCpf, formatarTelefone, somenteDigitos } from '../formatos'
import Botao from './Botao'
import CampoTexto from './CampoTexto'
import MensagemErro from './MensagemErro'
import Modal from './Modal'

const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Espelha as anotações do AlunoRequest para o erro aparecer antes de ir ao servidor.
// O back continua validando: isto é conforto, não garantia.
function validar({ nome, cpf, email, telefone }) {
  const erros = {}
  if (!nome.trim()) erros.nome = 'Informe o nome.'
  if (cpf.length !== 11) erros.cpf = 'O CPF deve ter 11 dígitos.'
  if (!email.trim()) erros.email = 'Informe o e-mail.'
  else if (!EMAIL.test(email.trim())) erros.email = 'E-mail inválido. Ex.: nome@dominio.com'
  if (telefone.length < 10) erros.telefone = 'Informe DDD + número (10 ou 11 dígitos).'
  return erros
}

// aluno = null → cadastro; aluno = objeto → edição desse aluno
export default function ModalAluno({ aluno, onFechar, onSalvo }) {
  const id = useId()
  const editando = Boolean(aluno)
  // CPF e telefone ficam no estado só com dígitos (o formato do AlunoRequest); a máscara é só na tela
  const inicial = {
    nome: aluno?.nome ?? '',
    cpf: aluno?.cpf ?? '',
    email: aluno?.email ?? '',
    telefone: aluno?.telefone ?? '',
  }
  const [dados, setDados] = useState(inicial)
  const [errosCampos, setErrosCampos] = useState({})
  const [erroGeral, setErroGeral] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const alterado = Object.keys(inicial).some(campo => dados[campo] !== inicial[campo])

  // alterar('nome') devolve a função que atualiza só o nome; editar o campo apaga o erro dele
  const alterar = campo => valor => {
    setDados(atual => ({ ...atual, [campo]: valor }))
    setErrosCampos(atual => ({ ...atual, [campo]: undefined }))
  }

  async function enviar(evento) {
    evento.preventDefault()

    const erros = validar(dados)
    const primeiroComErro = Object.keys(erros)[0]
    if (primeiroComErro) {
      setErrosCampos(erros)
      // leva o foco ao primeiro campo errado: quem usa teclado ou leitor de tela vai direto ao problema
      document.getElementById(`${id}-${primeiroComErro}`)?.focus()
      return
    }

    setEnviando(true)
    setErrosCampos({})
    setErroGeral(null)

    const corpo = { ...dados, nome: dados.nome.trim(), email: dados.email.trim() }

    try {
      const salvo = editando ? await alunosApi.atualizar(aluno.id, corpo) : await alunosApi.cadastrar(corpo)
      onSalvo(salvo, !editando)
    } catch (erro) {
      if (erro.campos.length > 0) {
        setErrosCampos(Object.fromEntries(erro.campos.map(c => [c.nome, c.mensagem])))
      } else {
        // 409 (CPF ou e-mail já usado), 404, servidor fora: a mensagem da API já diz qual campo
        setErroGeral(erro)
      }
      setEnviando(false)
    }
  }

  return (
    <Modal onFechar={onFechar} bloqueado={enviando} tituloId={`${id}-titulo`}>
      <h2 id={`${id}-titulo`}>{editando ? `Editar ${aluno.nome}` : 'Novo aluno'}</h2>

      <form onSubmit={enviar} noValidate>
        {erroGeral && (
          <div style={{ marginBottom: 20 }}>
            <MensagemErro erro={erroGeral} />
          </div>
        )}

        {/* autoComplete="off": quem digita é a recepção; o navegador não deve sugerir os dados dela */}
        <CampoTexto
          id={`${id}-nome`}
          rotulo="Nome"
          valor={dados.nome}
          onChange={alterar('nome')}
          erro={errosCampos.nome}
          maxLength={100}
          autoComplete="off"
        />
        <CampoTexto
          id={`${id}-cpf`}
          rotulo="CPF"
          valor={formatarCpf(dados.cpf)}
          onChange={valor => alterar('cpf')(somenteDigitos(valor).slice(0, 11))}
          erro={errosCampos.cpf}
          inputMode="numeric"
          placeholder="000.000.000-00"
          autoComplete="off"
        />
        <CampoTexto
          id={`${id}-email`}
          rotulo="E-mail"
          type="email"
          valor={dados.email}
          onChange={alterar('email')}
          erro={errosCampos.email}
          maxLength={100}
          autoComplete="off"
        />
        <CampoTexto
          id={`${id}-telefone`}
          rotulo="Telefone"
          type="tel"
          valor={formatarTelefone(dados.telefone)}
          onChange={valor => alterar('telefone')(somenteDigitos(valor).slice(0, 11))}
          erro={errosCampos.telefone}
          inputMode="numeric"
          placeholder="(00) 00000-0000"
          autoComplete="off"
        />

        <div className="modal-acoes">
          <Botao variante="secundario" onClick={onFechar} disabled={enviando}>Cancelar</Botao>
          <Botao type="submit" disabled={enviando || (editando && !alterado)}>
            {enviando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Cadastrar aluno'}
          </Botao>
        </div>
      </form>
    </Modal>
  )
}

import { useCallback, useEffect, useEffectEvent, useState } from 'react'

// Busca os dados de uma tela ao abri-la, de novo a cada recarregar() e sempre que a "chave" mudar.
// "buscar" é uma função que devolve uma Promise: alunosApi.listar, () => Promise.all([...]) etc.
// "chave" descreve o que está sendo buscado (ex.: a data da agenda): mudou a chave, busca de novo.
export function useDados(buscar, chave = '') {
  // a versão sobe a cada recarregar(); junto com a chave, identifica "esta busca"
  const [versao, setVersao] = useState(0)
  const idBusca = `${versao}|${chave}`

  // guarda o resultado junto com o id da busca que o produziu
  const [resultado, setResultado] = useState({ id: null, dados: null, erro: null })

  // useEffectEvent: o efeito sempre usa o "buscar" mais recente, sem que ele vire dependência.
  // Quem chama costuma passar uma função nova a cada render; como dependência, a busca se repetiria sem parar.
  const executarBusca = useEffectEvent(() => buscar())

  useEffect(() => {
    let ignorar = false

    executarBusca()
      .then(dados => {
        if (!ignorar) setResultado({ id: idBusca, dados, erro: null })
      })
      .catch(erro => {
        // em caso de erro, mantém os últimos dados que deram certo
        if (!ignorar) setResultado(anterior => ({ id: idBusca, dados: anterior.dados, erro }))
      })

    // limpeza: se a chave mudar, a tela recarregar ou a pessoa sair antes da resposta,
    // a resposta velha é descartada (sem isso, uma resposta lenta de ontem poderia sobrescrever a de hoje)
    return () => {
      ignorar = true
    }
  }, [idBusca])

  const recarregar = useCallback(() => setVersao(v => v + 1), [])

  // "carregando" é calculado, não guardado: está carregando enquanto o resultado não for desta busca
  const carregando = resultado.id !== idBusca

  return {
    // os últimos dados recebidos (durante uma nova busca, continuam os anteriores)
    dados: resultado.dados,
    carregando,
    // o erro só vale para a busca atual: ao tentar de novo, a mensagem antiga some
    erro: carregando ? null : resultado.erro,
    recarregar,
  }
}

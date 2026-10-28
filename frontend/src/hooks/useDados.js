import { useCallback, useEffect, useEffectEvent, useState } from 'react'

// Busca os dados de uma tela ao abri-la e de novo a cada recarregar().
// "buscar" é uma função que devolve uma Promise: alunosApi.listar, () => Promise.all([...]) etc.
export function useDados(buscar) {
  const [dados, setDados] = useState(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(null)
  // mudar a versão faz o efeito rodar de novo: é assim que a tela recarrega
  const [versao, setVersao] = useState(0)

  // useEffectEvent: o efeito sempre usa o "buscar" mais recente, sem que ele vire dependência.
  // Quem chama costuma passar uma função nova a cada render; como dependência, a busca se repetiria sem parar.
  const executarBusca = useEffectEvent(() => buscar())

  useEffect(() => {
    let ignorar = false

    executarBusca()
      .then(resultado => {
        if (!ignorar) setDados(resultado)
      })
      .catch(e => {
        if (!ignorar) setErro(e)
      })
      .finally(() => {
        if (!ignorar) setCarregando(false)
      })

    // limpeza: se a pessoa sair da tela (ou recarregar) antes da resposta, a resposta velha é descartada
    return () => {
      ignorar = true
    }
  }, [versao])

  // "carregando" e "erro" são reiniciados aqui, no evento que pede a nova busca, e não dentro do efeito
  const recarregar = useCallback(() => {
    setCarregando(true)
    setErro(null)
    setVersao(v => v + 1)
  }, [])

  return { dados, carregando, erro, recarregar }
}

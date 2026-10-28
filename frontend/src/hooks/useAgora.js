import { useEffect, useState } from 'react'

// A hora atual, atualizada a cada minuto: uma tela deixada aberta na recepção
// vê o agendamento passar de "em andamento" para "encerrado" sozinha.
export function useAgora(intervaloMs = 60_000) {
  const [agora, setAgora] = useState(() => new Date())

  useEffect(() => {
    const timer = setInterval(() => setAgora(new Date()), intervaloMs)
    // limpeza: ao sair da tela, o relógio para (senão ele continuaria rodando à toa)
    return () => clearInterval(timer)
  }, [intervaloMs])

  return agora
}

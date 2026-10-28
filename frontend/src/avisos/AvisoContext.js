import { createContext, useContext } from 'react'

export const AvisoContext = createContext(null)

// uso em qualquer componente: const avisar = useAviso(); avisar('Salvo.') ou avisar('Falhou.', 'erro')
export function useAviso() {
  return useContext(AvisoContext)
}
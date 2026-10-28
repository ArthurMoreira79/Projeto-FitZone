import { useCallback, useEffect, useState } from 'react'
import { AvisoContext } from '../avisos/AvisoContext'
import './AvisoProvider.css'

export default function AvisoProvider({ children }) {
  const [aviso, setAviso] = useState(null) // { mensagem, tipo: 'sucesso' | 'erro', id }

  const avisar = useCallback((mensagem, tipo = 'sucesso') => {
    // o id muda a cada aviso: dois avisos iguais seguidos ainda reiniciam o tempo
    setAviso({ mensagem, tipo, id: Date.now() })
  }, [])

  useEffect(() => {
    if (!aviso) return
    // erro fica mais tempo na tela: a pessoa precisa ler o que fazer
    const timer = setTimeout(() => setAviso(null), aviso.tipo === 'erro' ? 8000 : 4000)
    return () => clearTimeout(timer)
  }, [aviso])

  return (
    <AvisoContext.Provider value={avisar}>
      {children}

      <div className="aviso-regiao" role="status" aria-live="polite">
        {aviso && (
          <div key={aviso.id} className={`aviso aviso-${aviso.tipo}`}>
            <p>{aviso.mensagem}</p>
            <button type="button" className="aviso-fechar" onClick={() => setAviso(null)} aria-label="Fechar aviso">
              ×
            </button>
          </div>
        )}
      </div>
    </AvisoContext.Provider>
  )
}
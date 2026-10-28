import { useEffect, useState } from 'react'
import { ambientesApi } from './api/ambientes'

function App() {
  const [ambientes, setAmbientes] = useState([])
  const [erro, setErro] = useState(null)

  useEffect(() => {
    async function carregar() {
      try {
        setAmbientes(await ambientesApi.listar())
      } catch (e) {
        setErro(e)
      }
    }
    carregar()
  }, [])

  if (erro) {
    return (
      <div>
        <p>Erro {erro.status}: {erro.message}</p>
        {erro.campos?.length > 0 && (
          <ul>
            {erro.campos.map(c => <li key={c.nome}>{c.nome}: {c.mensagem}</li>)}
          </ul>
        )}
      </div>
    )
  }

  return (
    <main>
      <h1>FitZone — Ambientes</h1>
      {ambientes.length === 0
        ? <p>Nenhum ambiente cadastrado.</p>
        : (
          <ul>
            {ambientes.map(a => (
              <li key={a.id}>{a.id} — {a.nome} (R$ {a.valorHora}/h)</li>
            ))}
          </ul>
        )}
    </main>
  )
}

export default App
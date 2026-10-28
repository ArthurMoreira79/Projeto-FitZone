import { useEffect, useState } from 'react'
import { ambientesApi } from '../api/ambientes'
import CabecalhoPagina from '../components/CabecalhoPagina'

export default function Ambientes() {
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

  return (
    <>
      <CabecalhoPagina
        titulo="Ambientes"
        descricao="Salas e piscina disponíveis para reserva, com o valor cobrado por hora."
      />

      {erro && <p>{erro.message}</p>}

      {!erro && ambientes.length === 0 && <p>Nenhum ambiente cadastrado.</p>}

      {ambientes.length > 0 && (
        <ul>
          {ambientes.map(a => (
            <li key={a.id}>
              <span className="numero">{a.id}</span> — {a.nome} (R$ {a.valorHora}/h)
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
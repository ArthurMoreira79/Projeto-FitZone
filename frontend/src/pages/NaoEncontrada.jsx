import { Link, useLocation } from 'react-router'
import CabecalhoPagina from '../components/CabecalhoPagina'

export default function NaoEncontrada() {
  const { pathname } = useLocation()

  return (
    <>
      <CabecalhoPagina
        titulo="Página não encontrada"
        descricao={`O endereço ${pathname} não existe no FitZone.`}
      />
      <Link to="/">Ir para o painel</Link>
    </>
  )
}
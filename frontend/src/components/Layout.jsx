import { NavLink, Outlet } from 'react-router'
import './Layout.css'

const secoes = [
  { para: '/', rotulo: 'Painel' },
  { para: '/agendamentos', rotulo: 'Agendamentos' },
  { para: '/alunos', rotulo: 'Alunos' },
  { para: '/ambientes', rotulo: 'Ambientes' },
  { para: '/relatorios', rotulo: 'Relatórios' },
]

export default function Layout() {
  return (
    <div className="layout">
      <header className="topo">
        <div className="topo-conteudo">
          <NavLink to="/" className="marca">FitZone</NavLink>
          <nav className="nav" aria-label="Seções">
            {secoes.map(s => (
              // "end" no "/": sem ele, o Painel ficaria ativo em todas as rotas (todas começam com /)
              <NavLink key={s.para} to={s.para} end={s.para === '/'}>
                {s.rotulo}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>

      <main className="conteudo">
        <Outlet />
      </main>
    </div>
  )
}
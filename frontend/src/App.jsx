import { Route, Routes } from 'react-router'
import Layout from './components/Layout'
import Painel from './pages/Painel'
import Agendamentos from './pages/Agendamentos'
import Alunos from './pages/Alunos'
import Ambientes from './pages/Ambientes'
import Relatorios from './pages/Relatorios'
import NaoEncontrada from './pages/NaoEncontrada'

export default function App() {
  return (
    <Routes>
      {/* rota "pai" sem path: o Layout envolve todas as filhas, que aparecem no <Outlet /> */}
      <Route element={<Layout />}>
        <Route index element={<Painel />} />
        <Route path="agendamentos" element={<Agendamentos />} />
        <Route path="alunos" element={<Alunos />} />
        <Route path="ambientes" element={<Ambientes />} />
        <Route path="relatorios" element={<Relatorios />} />
        <Route path="*" element={<NaoEncontrada />} />
      </Route>
    </Routes>
  )
}
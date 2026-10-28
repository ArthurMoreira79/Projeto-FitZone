import { request } from './client'

export const relatoriosApi = {
  porAluno: (id) => request(`/relatorios/alunos/${id}`),
  porAmbiente: () => request('/relatorios/ambientes'),
  faturamento: () => request('/relatorios/faturamento'),
  porServico: () => request('/relatorios/servicos'),
}
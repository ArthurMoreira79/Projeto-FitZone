import { request } from './client'

export const ambientesApi = {
  listar: () => request('/ambientes'),
  buscar: (id) => request(`/ambientes/${id}`),
  cadastrar: (tipo, observacoes) =>
    request('/ambientes', { method: 'POST', body: { tipo, observacoes } }),
  atualizarObservacoes: (id, observacoes) =>
    request(`/ambientes/${id}`, { method: 'PUT', body: { observacoes } }),
  excluir: (id) => request(`/ambientes/${id}`, { method: 'DELETE' }),
}
import { request } from './client'

export const agendamentosApi = {
  listar: () => request('/agendamentos'),
  buscar: (id) => request(`/agendamentos/${id}`),
  // agendamento = { alunoId, ambienteId, data: 'AAAA-MM-DD', horaInicio: 'HH:mm', horaFim: 'HH:mm' }
  realizar: (agendamento) => request('/agendamentos', { method: 'POST', body: agendamento }),
  // servico = { tipo, quantidade } — quantidade só para LOCKER
  adicionarServico: (id, servico) =>
    request(`/agendamentos/${id}/servicos`, { method: 'POST', body: servico }),
  cancelar: (id) => request(`/agendamentos/${id}`, { method: 'DELETE' }),
}
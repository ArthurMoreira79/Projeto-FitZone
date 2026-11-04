import { request } from './client'

// monta "?data=...&ambienteId=..." só com os filtros preenchidos (vazio/null/undefined ficam de fora)
function queryString(filtros) {
  const params = new URLSearchParams()
  for (const [chave, valor] of Object.entries(filtros)) {
    if (valor !== undefined && valor !== null && valor !== '') params.set(chave, valor)
  }
  const texto = params.toString()
  return texto ? `?${texto}` : ''
}

export const agendamentosApi = {
  // filtros = { data: 'AAAA-MM-DD', ambienteId, alunoId } — todos opcionais
  listar: (filtros = {}) => request(`/agendamentos${queryString(filtros)}`),
  buscar: (id) => request(`/agendamentos/${id}`),
  // prévia calculada pela API (mesma regra de horas cobradas do agendamento real); nada é gravado
  orcar: (ambienteId, horaInicio, horaFim) =>
    request(`/agendamentos/orcamento${queryString({ ambienteId, horaInicio, horaFim })}`),
  // agendamento = { alunoId, ambienteId, data: 'AAAA-MM-DD', horaInicio: 'HH:mm', horaFim: 'HH:mm' }
  realizar: (agendamento) => request('/agendamentos', { method: 'POST', body: agendamento }),
  // servico = { tipo, quantidade } — quantidade só para LOCKER
  adicionarServico: (id, servico) =>
    request(`/agendamentos/${id}/servicos`, { method: 'POST', body: servico }),
  // devolve o agendamento com o total recalculado, como o adicionarServico
  removerServico: (id, servicoId) =>
    request(`/agendamentos/${id}/servicos/${servicoId}`, { method: 'DELETE' }),
  cancelar: (id) => request(`/agendamentos/${id}`, { method: 'DELETE' }),
}

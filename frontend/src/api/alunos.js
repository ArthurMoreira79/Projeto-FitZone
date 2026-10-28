import { request } from './client'

export const alunosApi = {
  listar: () => request('/alunos'),
  buscar: (id) => request(`/alunos/${id}`),
  // CPF vai na query string: URLSearchParams faz o escape dos caracteres
  buscarPorCpf: (cpf) => request(`/alunos/busca?${new URLSearchParams({ cpf })}`),
  // aluno = { cpf, nome, email, telefone } — mesmos campos do AlunoRequest
  cadastrar: (aluno) => request('/alunos', { method: 'POST', body: aluno }),
  atualizar: (id, aluno) => request(`/alunos/${id}`, { method: 'PUT', body: aluno }),
  excluir: (id) => request(`/alunos/${id}`, { method: 'DELETE' }),
}
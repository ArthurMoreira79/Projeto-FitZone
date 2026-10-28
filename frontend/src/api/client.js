const API_URL = import.meta.env.VITE_API_URL

// Equivalente JS do seu ErroResposta: todo erro da API vira um ApiError
export class ApiError extends Error {
  constructor(status, mensagem, campos = []) {
    super(mensagem)
    this.name = 'ApiError'
    this.status = status
    this.campos = campos
  }
}

export async function request(caminho, { method = 'GET', body } = {}) {
  const temCorpo = body !== undefined

  let resposta
  try {
    resposta = await fetch(`${API_URL}${caminho}`, {
      method,
      headers: temCorpo ? { 'Content-Type': 'application/json' } : undefined,
      body: temCorpo ? JSON.stringify(body) : undefined,
    })
  } catch {
    // fetch só cai aqui quando nem chegou resposta: API fora do ar ou CORS
    throw new ApiError(0, 'Não foi possível conectar ao servidor. Verifique se a API está rodando.')
  }

  // DELETE devolve 204 sem corpo: não há JSON para ler
  if (resposta.status === 204) return null

  // se o corpo não for JSON (ex.: erro de um proxy), segue com null em vez de quebrar
  const dados = await resposta.json().catch(() => null)

  if (!resposta.ok) {
    throw new ApiError(
      resposta.status,
      dados?.mensagem ?? `Erro inesperado (HTTP ${resposta.status}).`,
      dados?.campos ?? [],
    )
  }

  return dados
}
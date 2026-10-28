import { request } from './client'

export const servicosApi = {
  // catálogo: [{ codigo, nome, valor, cobranca: 'FIXO' | 'POR_HORA' | 'POR_UNIDADE' }]
  listarTipos: () => request('/servicos/tipos'),
}

// criado uma vez só: montar um Intl.NumberFormat é caro, usar é barato
const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

export const formatarMoeda = (valor) => moeda.format(valor)

export const somenteDigitos = (texto) => texto.replace(/\D/g, '')

// "12345678901" → "123.456.789-01"; funciona com o CPF pela metade (máscara enquanto digita)
export function formatarCpf(valor) {
  return somenteDigitos(valor)
    .slice(0, 11)
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d)/, '.$1-$2')
}

// 11 dígitos = celular "(11) 98765-4321"; 10 = fixo "(11) 3456-7890"; também funciona pela metade
export function formatarTelefone(valor) {
  const d = somenteDigitos(valor).slice(0, 11)
  if (d.length === 0) return ''
  if (d.length <= 2) return `(${d}`
  const ddd = d.slice(0, 2)
  const numero = d.slice(2)
  const corte = d.length === 11 ? 5 : 4
  if (numero.length <= corte) return `(${ddd}) ${numero}`
  return `(${ddd}) ${numero.slice(0, corte)}-${numero.slice(corte)}`
}

// "2026-10-28" → "28/10/2026". Sem new Date() de propósito: ele leria a data como meia-noite
// em UTC e, no fuso de Brasília (UTC-3), mostraria 27/10.
export function formatarData(iso) {
  const [ano, mes, dia] = iso.split('-')
  return `${dia}/${mes}/${ano}`
}

// para busca: "João" e "joao" passam a ser iguais (minúsculas, sem acento)
export const normalizar = (texto) => texto.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase()

// plural(1, 'aluno', 'alunos') → "1 aluno"; plural(3, ...) → "3 alunos"
export const plural = (n, singular, pluralizado) => `${n} ${n === 1 ? singular : pluralizado}`

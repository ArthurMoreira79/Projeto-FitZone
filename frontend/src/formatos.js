// criado uma vez só: montar um Intl.NumberFormat é caro, usar é barato
const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

export const formatarMoeda = (valor) => moeda.format(valor)
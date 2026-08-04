// Escala Goldmine de estado del disco (spec §8, D-1). Enum del backend -> sigla + explicacion.
export interface GoldmineInfo {
  sigla: string
  nombre: string
  descripcion: string
}

export const GOLDMINE: Record<string, GoldmineInfo> = {
  MINT: {
    sigla: 'M',
    nombre: 'Mint',
    descripcion: 'Perfecto, como nuevo. Sin ninguna señal de uso. Muy poco común.',
  },
  NEAR_MINT: {
    sigla: 'NM',
    nombre: 'Near Mint',
    descripcion: 'Casi perfecto. Se usó muy poco; sin marcas visibles significativas.',
  },
  VG_PLUS_PLUS: {
    sigla: 'VG++',
    nombre: 'Very Good Plus Plus',
    descripcion: 'Excelente estado. Marcas mínimas que no afectan la reproducción.',
  },
  VG_PLUS: {
    sigla: 'VG+',
    nombre: 'Very Good Plus',
    descripcion: 'Muy buen estado. Leves signos de uso; suena muy bien.',
  },
  VG: {
    sigla: 'VG',
    nombre: 'Very Good',
    descripcion: 'Uso notorio. Puede tener algún ruido de superficie audible.',
  },
  GOOD: {
    sigla: 'G',
    nombre: 'Good',
    descripcion: 'Se puede escuchar de principio a fin, pero con desgaste evidente.',
  },
  POOR: {
    sigla: 'P',
    nombre: 'Poor',
    descripcion: 'Muy desgastado. Solo para relleno o coleccionismo puntual.',
  },
}

export function goldmine(estado: string): GoldmineInfo {
  return GOLDMINE[estado] ?? { sigla: estado, nombre: estado, descripcion: '' }
}

// Orden de mejor a peor, para el glosario.
export const GOLDMINE_ORDEN = [
  'MINT',
  'NEAR_MINT',
  'VG_PLUS_PLUS',
  'VG_PLUS',
  'VG',
  'GOOD',
  'POOR',
]

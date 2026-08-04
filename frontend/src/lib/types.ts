// Tipos compartidos con el backend (DTOs).

export interface Genero {
  id: string
  nombre: string
}

export interface Foto {
  url: string
  orden: number
  esPortada: boolean
}

export interface ViniloResumen {
  id: string
  titulo: string
  artista: string
  genero: string | null
  anio: number | null
  precio: number
  estadoDisco: string
  estado: string
  senable: boolean
  descuentoCortePct: number
  portadaUrl: string | null
  fechaPublicacion: string
}

export interface ViniloDetalle {
  id: string
  titulo: string
  artista: string
  genero: Genero | null
  anio: number | null
  sello: string | null
  edicionPais: string | null
  formato: string
  estadoDisco: string
  descripcion: string | null
  precio: number
  descuentoCortePct: number
  senable: boolean
  estado: string
  fechaPublicacion: string
  fotos: Foto[]
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface Usuario {
  id: string
  nombre: string
  email: string
  rol: 'CLIENTE' | 'ADMIN'
  emailVerificado: boolean
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  usuario: Usuario
}

export interface Orden {
  id: string
  tipo: string
  estado: string
  total: number
  montoPagado: number | null
  codigoRetiro: string | null
  fechaVencimiento: string | null
  createdAt: string
}

export interface Cupon {
  id: string
  porcentaje: number
  estado: string
  fechaGeneracion: string
  fechaVencimiento: string
}

export interface CatalogFiltros {
  q?: string
  artista?: string
  generoId?: string
  precioMin?: string
  precioMax?: string
  estadoDisco?: string
  page?: number
  size?: number
}

// Tipos compartidos con el backend (DTOs).

export interface Genero {
  id: string
  nombre: string
}

export interface Foto {
  id: string
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
  estado: string
  modoEntrega: string
  total: number
  codigoRetiro: string | null
  createdAt: string
}

export type ModoEntrega = 'RETIRO' | 'ENVIO'

export interface Envio {
  nombre: string
  telefono: string
  direccion: string
  localidad: string
  provincia: string
  cp: string
}

export interface CrearOrdenRequest {
  viniloIds: string[]
  modoEntrega: ModoEntrega
  envio?: Envio | null
}

export interface CheckoutResponse {
  ordenId: string
  checkoutUrl: string
}

export interface Cupon {
  id: string
  porcentaje: number
  estado: string
  fechaGeneracion: string
  fechaVencimiento: string
}

export interface ViniloFormData {
  titulo: string
  artista: string
  generoId?: string | null
  anio?: number | null
  sello?: string | null
  edicionPais?: string | null
  estadoDisco: string
  descripcion?: string | null
  precio: number
  descuentoCortePct: number
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

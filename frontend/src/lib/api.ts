// Cliente HTTP hacia el backend. Maneja el Bearer token y el auto-refresh en 401.
import type {
  AuthResponse,
  CheckoutResponse,
  CrearOrdenRequest,
  Cupon,
  Foto,
  Genero,
  Orden,
  OrdenAdmin,
  PageResponse,
  Usuario,
  ViniloDetalle,
  ViniloFormData,
  ViniloResumen,
  CatalogFiltros,
} from './types'

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

const ACCESS_KEY = 'pw_access'
const REFRESH_KEY = 'pw_refresh'

export const tokenStore = {
  access: () => localStorage.getItem(ACCESS_KEY),
  refresh: () => localStorage.getItem(REFRESH_KEY),
  set(access: string, refresh: string) {
    localStorage.setItem(ACCESS_KEY, access)
    localStorage.setItem(REFRESH_KEY, refresh)
  },
  clear() {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
  },
}

let onAuthFailure: (() => void) | null = null
export function setOnAuthFailure(cb: () => void) {
  onAuthFailure = cb
}

export class ApiError extends Error {
  status: number
  details?: Record<string, string>
  constructor(status: number, message: string, details?: Record<string, string>) {
    super(message)
    this.status = status
    this.details = details
  }
}

async function toError(res: Response): Promise<ApiError> {
  let message = `Error ${res.status}`
  let details: Record<string, string> | undefined
  try {
    const body = await res.json()
    if (body?.error) message = body.error
    if (body?.details) details = body.details
  } catch {
    /* respuesta sin cuerpo JSON */
  }
  return new ApiError(res.status, message, details)
}

async function tryRefresh(): Promise<boolean> {
  const refresh = tokenStore.refresh()
  if (!refresh) return false
  try {
    const res = await fetch(`${API_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: refresh }),
    })
    if (!res.ok) return false
    const data: AuthResponse = await res.json()
    tokenStore.set(data.accessToken, data.refreshToken)
    return true
  } catch {
    return false
  }
}

interface Opts {
  method?: string
  body?: unknown
  auth?: boolean
}

async function request<T>(path: string, opts: Opts = {}, retry = true): Promise<T> {
  const headers: Record<string, string> = {}
  if (opts.body !== undefined) headers['Content-Type'] = 'application/json'
  if (opts.auth) {
    const token = tokenStore.access()
    if (token) headers.Authorization = `Bearer ${token}`
  }

  const res = await fetch(`${API_URL}${path}`, {
    method: opts.method ?? 'GET',
    headers,
    body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined,
  })

  if (res.status === 401 && opts.auth && retry && tokenStore.refresh()) {
    const ok = await tryRefresh()
    if (ok) return request<T>(path, opts, false)
    tokenStore.clear()
    onAuthFailure?.()
    throw new ApiError(401, 'Sesión expirada')
  }

  if (!res.ok) throw await toError(res)
  if (res.status === 204) return undefined as T
  return (await res.json()) as T
}

/** Igual que request pero con FormData (multipart), para subir fotos. */
async function requestMultipart<T>(path: string, formData: FormData, retry = true): Promise<T> {
  const headers: Record<string, string> = {}
  const token = tokenStore.access()
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(`${API_URL}${path}`, { method: 'POST', headers, body: formData })

  if (res.status === 401 && retry && tokenStore.refresh()) {
    const ok = await tryRefresh()
    if (ok) return requestMultipart<T>(path, formData, false)
    tokenStore.clear()
    onAuthFailure?.()
    throw new ApiError(401, 'Sesión expirada')
  }
  if (!res.ok) throw await toError(res)
  return (await res.json()) as T
}

function qs(filtros: CatalogFiltros): string {
  const p = new URLSearchParams()
  Object.entries(filtros).forEach(([k, v]) => {
    if (v !== undefined && v !== null && `${v}` !== '') p.set(k, `${v}`)
  })
  const s = p.toString()
  return s ? `?${s}` : ''
}

export const authApi = {
  register: (body: { nombre: string; email: string; password: string; telefono?: string }) =>
    request<{ message: string }>('/auth/register', { method: 'POST', body }),
  login: (body: { email: string; password: string }) =>
    request<AuthResponse>('/auth/login', { method: 'POST', body }),
  google: (idToken: string) =>
    request<AuthResponse>('/auth/google', { method: 'POST', body: { idToken } }),
  verify: (token: string) =>
    request<{ message: string }>(`/auth/verify?token=${encodeURIComponent(token)}`),
  me: () => request<Usuario>('/auth/me', { auth: true }),
}

export const catalogApi = {
  list: (filtros: CatalogFiltros) => request<PageResponse<ViniloResumen>>(`/vinilos${qs(filtros)}`),
  get: (id: string) => request<ViniloDetalle>(`/vinilos/${id}`),
  generos: () => request<Genero[]>('/generos'),
}

export const accountApi = {
  ordenes: () => request<Orden[]>('/ordenes/mias', { auth: true }),
  cupones: () => request<Cupon[]>('/cupones/mios', { auth: true }),
}

export const orderApi = {
  crear: (body: CrearOrdenRequest) =>
    request<CheckoutResponse>('/ordenes', { method: 'POST', body, auth: true }),
  // Dev: simula la confirmación del pago (reemplaza el webhook de MP).
  simularPago: (ordenId: string, aprobado: boolean) =>
    request<{ message: string }>(
      `/dev/pagos/simular?orden=${ordenId}&aprobado=${aprobado}`,
      { method: 'POST' },
    ),
}

export const adminApi = {
  listVinilos: (page = 0, size = 100) =>
    request<PageResponse<ViniloResumen>>(`/admin/vinilos?page=${page}&size=${size}`, { auth: true }),
  getVinilo: (id: string) => request<ViniloDetalle>(`/admin/vinilos/${id}`, { auth: true }),
  crearVinilo: (body: ViniloFormData) => request<ViniloDetalle>('/vinilos', { method: 'POST', body, auth: true }),
  actualizarVinilo: (id: string, body: ViniloFormData) =>
    request<ViniloDetalle>(`/vinilos/${id}`, { method: 'PUT', body, auth: true }),
  pausar: (id: string) => request<ViniloDetalle>(`/vinilos/${id}/pausar`, { method: 'PATCH', auth: true }),
  subirFotos: (id: string, files: File[]) => {
    const fd = new FormData()
    files.forEach((f) => fd.append('files', f))
    return requestMultipart<Foto[]>(`/vinilos/${id}/fotos`, fd)
  },
  borrarFoto: (id: string, fotoId: string) =>
    request<void>(`/vinilos/${id}/fotos/${fotoId}`, { method: 'DELETE', auth: true }),
  crearGenero: (nombre: string) => request<Genero>('/generos', { method: 'POST', body: { nombre }, auth: true }),
  // Órdenes / entrega (M4)
  listOrdenes: (page = 0, size = 50) =>
    request<PageResponse<OrdenAdmin>>(`/admin/ordenes?page=${page}&size=${size}`, { auth: true }),
  buscarRetiro: (codigo: string) => request<OrdenAdmin>(`/retiros/${encodeURIComponent(codigo)}`, { auth: true }),
  entregar: (codigo: string) =>
    request<OrdenAdmin>(`/retiros/${encodeURIComponent(codigo)}/entregar`, { method: 'POST', auth: true }),
  despachar: (ordenId: string) =>
    request<OrdenAdmin>(`/admin/ordenes/${ordenId}/despachar`, { method: 'POST', auth: true }),
  ventaEfectivo: (viniloId: string) =>
    request<ViniloDetalle>(`/vinilos/${viniloId}/venta-efectivo`, { method: 'POST', auth: true }),
}

export interface HealthResponse {
  status: string
  service: string
  timestamp: string
  database: string
}
export const getHealth = () => request<HealthResponse>('/api/health')

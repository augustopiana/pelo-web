import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi, setOnAuthFailure, tokenStore } from '../lib/api'
import type { AuthResponse, Usuario } from '../lib/types'

interface AuthState {
  usuario: Usuario | null
  cargando: boolean
  login: (email: string, password: string) => Promise<void>
  aplicarSesion: (resp: AuthResponse) => void
  logout: () => void
  refrescarUsuario: () => Promise<void>
}

const AuthContext = createContext<AuthState | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [cargando, setCargando] = useState(true)

  const logout = useCallback(() => {
    tokenStore.clear()
    setUsuario(null)
  }, [])

  // Si el refresh falla, la sesión se cae.
  useEffect(() => {
    setOnAuthFailure(() => setUsuario(null))
  }, [])

  // Al montar: si hay token, recuperar el usuario.
  useEffect(() => {
    if (!tokenStore.access()) {
      setCargando(false)
      return
    }
    authApi
      .me()
      .then(setUsuario)
      .catch(() => tokenStore.clear())
      .finally(() => setCargando(false))
  }, [])

  const aplicarSesion = useCallback((resp: AuthResponse) => {
    tokenStore.set(resp.accessToken, resp.refreshToken)
    setUsuario(resp.usuario)
  }, [])

  const login = useCallback(
    async (email: string, password: string) => {
      const resp = await authApi.login({ email, password })
      aplicarSesion(resp)
    },
    [aplicarSesion],
  )

  const refrescarUsuario = useCallback(async () => {
    const u = await authApi.me()
    setUsuario(u)
  }, [])

  const value = useMemo(
    () => ({ usuario, cargando, login, aplicarSesion, logout, refrescarUsuario }),
    [usuario, cargando, login, aplicarSesion, logout, refrescarUsuario],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  return ctx
}

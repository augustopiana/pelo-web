import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from './AuthContext'

/** Envuelve rutas que requieren sesión (y opcionalmente rol ADMIN). */
export function ProtectedRoute({ children, adminOnly }: { children: ReactNode; adminOnly?: boolean }) {
  const { usuario, cargando } = useAuth()

  if (cargando) return <div className="spinner">Cargando…</div>
  if (!usuario) return <Navigate to="/login" replace />
  if (adminOnly && usuario.rol !== 'ADMIN') return <Navigate to="/catalogo" replace />

  return <>{children}</>
}

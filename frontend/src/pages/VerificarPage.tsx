import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { authApi, ApiError } from '../lib/api'
import { useAuth } from '../auth/AuthContext'

export function VerificarPage() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const { usuario, refrescarUsuario } = useAuth()
  const [estado, setEstado] = useState<'cargando' | 'ok' | 'error'>('cargando')
  const [mensaje, setMensaje] = useState('')
  const yaCorrio = useRef(false)

  useEffect(() => {
    if (yaCorrio.current) return
    yaCorrio.current = true

    if (!token) {
      setEstado('error')
      setMensaje('Falta el token de verificación en el link.')
      return
    }
    authApi
      .verify(token)
      .then(async (r) => {
        setEstado('ok')
        setMensaje(r.message)
        if (usuario) await refrescarUsuario().catch(() => {})
      })
      .catch((e: unknown) => {
        setEstado('error')
        setMensaje(e instanceof ApiError ? e.message : 'No se pudo verificar la cuenta')
      })
  }, [token, usuario, refrescarUsuario])

  return (
    <div className="auth-wrap">
      <h1>Verificación de email</h1>
      <div className="card">
        {estado === 'cargando' && <div className="spinner">Verificando…</div>}
        {estado === 'ok' && <div className="alert alert--ok">{mensaje}</div>}
        {estado === 'error' && <div className="alert alert--error">{mensaje}</div>}
        <Link to={usuario ? '/cuenta' : '/login'} className="btn btn--block" style={{ marginTop: '0.5rem' }}>
          {usuario ? 'Ir a mi cuenta' : 'Ir a ingresar'}
        </Link>
      </div>
    </div>
  )
}

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { ApiError, authApi } from '../lib/api'
import { GoogleSignInButton } from '../components/GoogleSignInButton'

const GOOGLE_HABILITADO = !!import.meta.env.VITE_GOOGLE_CLIENT_ID

export function LoginPage() {
  const { login, aplicarSesion } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const ingresarConGoogle = async (idToken: string) => {
    setError(null)
    try {
      const resp = await authApi.google(idToken)
      aplicarSesion(resp)
      navigate('/cuenta')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo ingresar con Google')
    }
  }

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setEnviando(true)
    try {
      await login(email, password)
      navigate('/cuenta')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo iniciar sesión')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="auth-wrap">
      <h1>Ingresar</h1>
      <div className="card">
        {error && <div className="alert alert--error">{error}</div>}
        <form onSubmit={submit}>
          <div className="field">
            <label>Email</label>
            <input className="input" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className="field">
            <label>Contraseña</label>
            <input className="input" type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
          </div>
          <button className="btn btn--neon btn--block" disabled={enviando}>
            {enviando ? 'Ingresando…' : 'Ingresar'}
          </button>
        </form>

        {GOOGLE_HABILITADO ? (
          <GoogleSignInButton onCredential={ingresarConGoogle} />
        ) : (
          <button className="btn btn--block" style={{ marginTop: '0.6rem' }} disabled title="Se habilita al configurar las credenciales de Google">
            Ingresar con Google — próximamente
          </button>
        )}

        <p className="muted" style={{ fontSize: '0.85rem', marginTop: '1rem' }}>
          ¿No tenés cuenta? <Link to="/registro" className="link-neon">Creá una</Link>
        </p>
      </div>
    </div>
  )
}

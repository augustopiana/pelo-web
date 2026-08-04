import { useState } from 'react'
import { Link } from 'react-router-dom'
import { authApi, ApiError } from '../lib/api'

export function RegistroPage() {
  const [form, setForm] = useState({ nombre: '', email: '', password: '', telefono: '' })
  const [error, setError] = useState<string | null>(null)
  const [detalles, setDetalles] = useState<Record<string, string> | undefined>()
  const [ok, setOk] = useState(false)
  const [enviando, setEnviando] = useState(false)

  const set = (campo: keyof typeof form, valor: string) => setForm((f) => ({ ...f, [campo]: valor }))

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setDetalles(undefined)
    setEnviando(true)
    try {
      await authApi.register({
        nombre: form.nombre,
        email: form.email,
        password: form.password,
        telefono: form.telefono || undefined,
      })
      setOk(true)
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
        setDetalles(err.details)
      } else {
        setError('No se pudo crear la cuenta')
      }
    } finally {
      setEnviando(false)
    }
  }

  if (ok) {
    return (
      <div className="auth-wrap">
        <h1>Revisá tu email</h1>
        <div className="card">
          <div className="alert alert--ok">
            Te enviamos un link de verificación a <strong>{form.email}</strong>. Verificá tu cuenta para poder señar o comprar.
          </div>
          <p className="muted" style={{ fontSize: '0.85rem' }}>
            (En desarrollo el link aparece en la consola del backend.)
          </p>
          <Link to="/login" className="btn btn--block">Ir a ingresar</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="auth-wrap">
      <h1>Crear cuenta</h1>
      <div className="card">
        {error && (
          <div className="alert alert--error">
            {error}
            {detalles && (
              <ul style={{ margin: '0.4rem 0 0', paddingLeft: '1.1rem' }}>
                {Object.entries(detalles).map(([k, v]) => (
                  <li key={k}>{k}: {v}</li>
                ))}
              </ul>
            )}
          </div>
        )}
        <form onSubmit={submit}>
          <div className="field">
            <label>Nombre</label>
            <input className="input" required value={form.nombre} onChange={(e) => set('nombre', e.target.value)} />
          </div>
          <div className="field">
            <label>Email</label>
            <input className="input" type="email" required value={form.email} onChange={(e) => set('email', e.target.value)} />
          </div>
          <div className="field">
            <label>Contraseña (mín. 8)</label>
            <input className="input" type="password" required minLength={8} value={form.password} onChange={(e) => set('password', e.target.value)} />
          </div>
          <div className="field">
            <label>Teléfono (opcional)</label>
            <input className="input" value={form.telefono} onChange={(e) => set('telefono', e.target.value)} />
          </div>
          <button className="btn btn--neon btn--block" disabled={enviando}>
            {enviando ? 'Creando…' : 'Crear cuenta'}
          </button>
        </form>
        <p className="muted" style={{ fontSize: '0.85rem', marginTop: '1rem' }}>
          ¿Ya tenés cuenta? <Link to="/login" className="link-neon">Ingresá</Link>
        </p>
      </div>
    </div>
  )
}

import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { orderApi, ApiError } from '../lib/api'

/**
 * Página de simulación de pago (solo dev). Reemplaza al checkout de Mercado Pago:
 * permite "aprobar" o "rechazar" el pago, que dispara la confirmación en el backend.
 */
export function CheckoutSimularPage() {
  const [params] = useSearchParams()
  const orden = params.get('orden')
  const navigate = useNavigate()
  const [estado, setEstado] = useState<'idle' | 'procesando' | 'listo'>('idle')
  const [mensaje, setMensaje] = useState('')
  const [error, setError] = useState<string | null>(null)

  const resolver = async (aprobado: boolean) => {
    if (!orden) return
    setError(null)
    setEstado('procesando')
    try {
      const r = await orderApi.simularPago(orden, aprobado)
      setMensaje(r.message)
      setEstado('listo')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo procesar el pago')
      setEstado('idle')
    }
  }

  if (!orden) return <div className="alert alert--error">Falta el identificador de la orden.</div>

  return (
    <section className="auth-wrap">
      <h1>Pago (simulado)</h1>
      <div className="card">
        <p className="muted" style={{ fontSize: '0.85rem' }}>
          Esto reemplaza el checkout de Mercado Pago en desarrollo. En producción, acá iría el pago real.
        </p>

        {error && <div className="alert alert--error">{error}</div>}

        {estado !== 'listo' ? (
          <div style={{ display: 'flex', gap: '0.6rem', marginTop: '0.5rem' }}>
            <button className="btn btn--neon btn--block" disabled={estado === 'procesando'} onClick={() => resolver(true)}>
              Aprobar pago
            </button>
            <button className="btn btn--block" disabled={estado === 'procesando'} onClick={() => resolver(false)}>
              Rechazar
            </button>
          </div>
        ) : (
          <>
            <div className="alert alert--ok">{mensaje}</div>
            <button className="btn btn--neon btn--block" onClick={() => navigate('/cuenta')}>
              Ir a mis órdenes
            </button>
          </>
        )}
      </div>
      <p className="muted" style={{ fontSize: '0.82rem', marginTop: '0.75rem' }}>
        <Link to="/catalogo" className="link-neon">Volver al catálogo</Link>
      </p>
    </section>
  )
}

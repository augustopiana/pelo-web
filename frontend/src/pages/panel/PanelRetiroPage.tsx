import { useState } from 'react'
import { Link } from 'react-router-dom'
import { adminApi, ApiError } from '../../lib/api'
import type { OrdenAdmin } from '../../lib/types'
import { precio } from '../../lib/format'

export function PanelRetiroPage() {
  const [codigo, setCodigo] = useState('')
  const [orden, setOrden] = useState<OrdenAdmin | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [buscando, setBuscando] = useState(false)
  const [entregando, setEntregando] = useState(false)

  const buscar = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setOrden(null)
    setBuscando(true)
    try {
      setOrden(await adminApi.buscarRetiro(codigo.trim()))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Error')
    } finally {
      setBuscando(false)
    }
  }

  const entregar = async () => {
    if (!orden) return
    setEntregando(true)
    setError(null)
    try {
      setOrden(await adminApi.entregar(orden.codigoRetiro!))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo entregar')
    } finally {
      setEntregando(false)
    }
  }

  return (
    <section>
      <Link to="/panel" className="upper muted">← Volver al panel</Link>
      <h1 style={{ marginTop: '0.75rem' }}>Retiro en el local</h1>

      <form onSubmit={buscar} className="genero-form">
        <input className="input" placeholder="Código de retiro (ej. H7K-2P9)" value={codigo} onChange={(e) => setCodigo(e.target.value)} required />
        <button className="btn btn--neon" disabled={buscando}>{buscando ? '…' : 'Buscar'}</button>
      </form>

      {error && <div className="alert alert--error">{error}</div>}

      {orden && (
        <div className="card" style={{ maxWidth: 560 }}>
          <div className="panel-row__tags" style={{ marginBottom: '0.6rem' }}>
            <span className={`tag tag--${orden.estado.toLowerCase()}`}>{orden.estado}</span>
            <span className="tag">{orden.modoEntrega === 'RETIRO' ? 'Retiro' : 'Envío'}</span>
            <span className="muted">{precio(orden.total)}</span>
          </div>
          <div className="muted" style={{ fontSize: '0.85rem', marginBottom: '0.5rem' }}>
            Cliente: {orden.clienteNombre} · {orden.clienteEmail}
          </div>
          <ul className="ficha__meta" style={{ margin: '0 0 0.75rem' }}>
            {orden.items.map((it) => (
              <li key={it.viniloId}><span>{it.titulo}</span><span>{it.artista} · {precio(it.precio)}</span></li>
            ))}
          </ul>
          {orden.estado === 'PAGADA' && orden.modoEntrega === 'RETIRO' ? (
            <button className="btn btn--neon btn--block" onClick={entregar} disabled={entregando}>
              {entregando ? 'Confirmando…' : 'Confirmar entrega'}
            </button>
          ) : orden.estado === 'ENTREGADA' ? (
            <div className="alert alert--ok">Entregado ✓</div>
          ) : (
            <p className="muted">Esta orden no está lista para entregar (estado {orden.estado}).</p>
          )}
        </div>
      )}
    </section>
  )
}

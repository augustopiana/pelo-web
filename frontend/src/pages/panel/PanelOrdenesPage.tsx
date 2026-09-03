import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { adminApi, ApiError } from '../../lib/api'
import type { OrdenAdmin } from '../../lib/types'
import { precio } from '../../lib/format'

export function PanelOrdenesPage() {
  const [ordenes, setOrdenes] = useState<OrdenAdmin[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const cargar = () => {
    setCargando(true)
    adminApi
      .listOrdenes()
      .then((p) => setOrdenes(p.content))
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Error'))
      .finally(() => setCargando(false))
  }

  useEffect(cargar, [])

  const despachar = async (id: string) => {
    try {
      const actualizada = await adminApi.despachar(id)
      setOrdenes((prev) => prev.map((o) => (o.id === id ? actualizada : o)))
    } catch (err) {
      alert(err instanceof ApiError ? err.message : 'No se pudo despachar')
    }
  }

  return (
    <section>
      <Link to="/panel" className="upper muted">← Volver al panel</Link>
      <h1 style={{ marginTop: '0.75rem' }}>Órdenes</h1>

      {cargando && <div className="spinner">Cargando…</div>}
      {error && <div className="alert alert--error">{error}</div>}

      {!cargando && !error && (
        <div className="panel-list">
          {ordenes.length === 0 && <p className="muted">Todavía no hay órdenes.</p>}
          {ordenes.map((o) => (
            <div className="panel-row" key={o.id} style={{ alignItems: 'flex-start' }}>
              <div className="panel-row__info">
                <div className="panel-row__tags">
                  <span className={`tag tag--${o.estado.toLowerCase()}`}>{o.estado}</span>
                  <span className="tag">{o.modoEntrega === 'RETIRO' ? 'Retiro' : 'Envío'}</span>
                  <span className="muted">{precio(o.total)}</span>
                  {o.codigoRetiro && <span className="muted">código {o.codigoRetiro}</span>}
                </div>
                <span className="muted" style={{ fontSize: '0.82rem' }}>
                  {o.clienteNombre} · {o.clienteEmail}
                  {o.clienteTelefono ? ` · ${o.clienteTelefono}` : ''}
                </span>
                <span style={{ fontSize: '0.85rem' }}>
                  {o.items.map((it) => it.titulo).join(', ')}
                </span>
                {o.modoEntrega === 'ENVIO' && o.envio && (
                  <span className="muted" style={{ fontSize: '0.82rem' }}>
                    📦 {o.envio.nombre} · {o.envio.direccion}, {o.envio.localidad}, {o.envio.provincia} ({o.envio.cp}) · {o.envio.telefono}
                  </span>
                )}
              </div>
              <div className="panel-row__actions">
                {o.estado === 'PAGADA' && o.modoEntrega === 'ENVIO' && (
                  <button className="btn btn--neon" onClick={() => despachar(o.id)}>Marcar despachado</button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

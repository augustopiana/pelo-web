import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { catalogApi, orderApi, ApiError } from '../lib/api'
import type { ModoEntrega, ViniloDetalle } from '../lib/types'
import { precio } from '../lib/format'

const ENVIO_VACIO = { nombre: '', telefono: '', direccion: '', localidad: '', provincia: '', cp: '' }

export function CheckoutPage() {
  const { viniloId } = useParams<{ viniloId: string }>()
  const [v, setV] = useState<ViniloDetalle | null>(null)
  const [modo, setModo] = useState<ModoEntrega>('RETIRO')
  const [envio, setEnvio] = useState({ ...ENVIO_VACIO })
  const [error, setError] = useState<string | null>(null)
  const [procesando, setProcesando] = useState(false)

  useEffect(() => {
    if (!viniloId) return
    catalogApi.get(viniloId).then(setV).catch((e: unknown) => setError(e instanceof Error ? e.message : 'Error'))
  }, [viniloId])

  const setEnvioCampo = (campo: keyof typeof envio, valor: string) =>
    setEnvio((e) => ({ ...e, [campo]: valor }))

  const pagar = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!viniloId) return
    setError(null)
    setProcesando(true)
    try {
      const resp = await orderApi.crear({
        viniloIds: [viniloId],
        modoEntrega: modo,
        envio: modo === 'ENVIO' ? envio : null,
      })
      // Redirige al checkout (dev: página de simulación; MP real: checkout de MP).
      window.location.assign(resp.checkoutUrl)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo iniciar la compra')
      setProcesando(false)
    }
  }

  if (error && !v) return <div className="alert alert--error">{error}</div>
  if (!v) return <div className="spinner">Cargando…</div>

  return (
    <section className="auth-wrap" style={{ maxWidth: 520 }}>
      <Link to={`/catalogo/${v.id}`} className="upper muted">← Volver a la ficha</Link>
      <h1 style={{ marginTop: '0.75rem' }}>Comprar</h1>

      <div className="card" style={{ marginBottom: '1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <div>
            <strong>{v.titulo}</strong>
            <div className="muted">{v.artista}</div>
          </div>
          <div className="price">{precio(v.precio)}</div>
        </div>
      </div>

      {error && <div className="alert alert--error">{error}</div>}

      <form onSubmit={pagar} className="card">
        <div className="field">
          <label>Entrega</label>
          <select className="input" value={modo} onChange={(e) => setModo(e.target.value as ModoEntrega)}>
            <option value="RETIRO">Retiro en el local</option>
            <option value="ENVIO">Envío por correo</option>
          </select>
        </div>

        {modo === 'ENVIO' && (
          <>
            <div className="field"><label>Nombre de quien recibe</label>
              <input className="input" required value={envio.nombre} onChange={(e) => setEnvioCampo('nombre', e.target.value)} /></div>
            <div className="field"><label>Teléfono</label>
              <input className="input" required value={envio.telefono} onChange={(e) => setEnvioCampo('telefono', e.target.value)} /></div>
            <div className="field"><label>Dirección</label>
              <input className="input" required value={envio.direccion} onChange={(e) => setEnvioCampo('direccion', e.target.value)} /></div>
            <div className="field"><label>Localidad</label>
              <input className="input" required value={envio.localidad} onChange={(e) => setEnvioCampo('localidad', e.target.value)} /></div>
            <div className="field"><label>Provincia</label>
              <input className="input" required value={envio.provincia} onChange={(e) => setEnvioCampo('provincia', e.target.value)} /></div>
            <div className="field"><label>Código postal</label>
              <input className="input" required value={envio.cp} onChange={(e) => setEnvioCampo('cp', e.target.value)} /></div>
            <p className="muted" style={{ fontSize: '0.8rem' }}>
              El envío lo despacha el local por correo. Pagás online solo el precio del disco.
            </p>
          </>
        )}

        <button className="btn btn--neon btn--block" disabled={procesando}>
          {procesando ? 'Redirigiendo al pago…' : `Pagar ${precio(v.precio)}`}
        </button>
      </form>
    </section>
  )
}

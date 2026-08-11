import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { adminApi } from '../../lib/api'
import type { ViniloResumen } from '../../lib/types'
import { precio } from '../../lib/format'

export function PanelVinilosPage() {
  const [vinilos, setVinilos] = useState<ViniloResumen[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const cargar = () => {
    setCargando(true)
    adminApi
      .listVinilos()
      .then((p) => setVinilos(p.content))
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Error'))
      .finally(() => setCargando(false))
  }

  useEffect(cargar, [])

  const togglePausa = async (id: string) => {
    try {
      const v = await adminApi.pausar(id)
      setVinilos((prev) => prev.map((x) => (x.id === id ? { ...x, estado: v.estado } : x)))
    } catch (e) {
      alert(e instanceof Error ? e.message : 'No se pudo cambiar el estado')
    }
  }

  return (
    <section>
      <div className="panel-head">
        <h1>Vinilos</h1>
        <div className="panel-head__actions">
          <Link to="/panel/generos" className="btn">Géneros</Link>
          <Link to="/panel/vinilos/nuevo" className="btn btn--neon">+ Nuevo vinilo</Link>
        </div>
      </div>

      {cargando && <div className="spinner">Cargando…</div>}
      {error && <div className="alert alert--error">{error}</div>}

      {!cargando && !error && (
        <div className="panel-list">
          {vinilos.length === 0 && <p className="muted">Todavía no hay vinilos. Cargá el primero.</p>}
          {vinilos.map((v) => (
            <div className="panel-row" key={v.id}>
              <div className="panel-row__cover">
                {v.portadaUrl ? <img src={v.portadaUrl} alt="" /> : <span className="panel-row__nofoto">sin foto</span>}
              </div>
              <div className="panel-row__info">
                <strong>{v.titulo}</strong>
                <span className="muted">{v.artista}</span>
                <div className="panel-row__tags">
                  <span className={`tag tag--${v.estado.toLowerCase()}`}>{v.estado}</span>
                  <span className="tag">{v.senable ? 'Señable' : 'Compra directa'}</span>
                  <span className="muted">{precio(v.precio)}</span>
                </div>
              </div>
              <div className="panel-row__actions">
                <Link to={`/panel/vinilos/${v.id}`} className="btn">Editar</Link>
                {(v.estado === 'DISPONIBLE' || v.estado === 'PAUSADO') && (
                  <button className="btn" onClick={() => togglePausa(v.id)}>
                    {v.estado === 'PAUSADO' ? 'Reactivar' : 'Pausar'}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

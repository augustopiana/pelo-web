import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { catalogApi } from '../lib/api'
import type { ViniloDetalle } from '../lib/types'
import { precio } from '../lib/format'
import { goldmine } from '../lib/goldmine'
import { GoldmineBadge } from '../components/GoldmineBadge'
import { useAuth } from '../auth/AuthContext'

export function FichaPage() {
  const { id } = useParams<{ id: string }>()
  const { usuario } = useAuth()
  const [v, setV] = useState<ViniloDetalle | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [fotoActiva, setFotoActiva] = useState(0)

  useEffect(() => {
    if (!id) return
    setError(null)
    catalogApi
      .get(id)
      .then((d) => {
        setV(d)
        setFotoActiva(0)
      })
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Error'))
  }, [id])

  if (error) return <div className="alert alert--error">{error}</div>
  if (!v) return <div className="spinner">Cargando…</div>

  const disponible = v.estado === 'DISPONIBLE'
  const fotoPrincipal = v.fotos[fotoActiva]?.url ?? v.fotos[0]?.url

  return (
    <section>
      <Link to="/catalogo" className="upper muted">← Volver al catálogo</Link>

      <div className="ficha" style={{ marginTop: '1rem' }}>
        <div>
          <div className="gallery__main">
            {fotoPrincipal && <img src={fotoPrincipal} alt={`${v.titulo} — ${v.artista}`} />}
          </div>
          {v.fotos.length > 1 && (
            <div className="gallery__thumbs">
              {v.fotos.map((f, i) => (
                <button
                  key={i}
                  className={`gallery__thumb ${i === fotoActiva ? 'active' : ''}`}
                  onClick={() => setFotoActiva(i)}
                  aria-label={`Foto ${i + 1}`}
                >
                  <img src={f.url} alt="" />
                </button>
              ))}
            </div>
          )}
        </div>

        <div>
          <h1 style={{ marginBottom: '0.15em' }}>{v.titulo}</h1>
          <p className="muted" style={{ fontSize: '1.1rem', marginTop: 0 }}>{v.artista}</p>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', margin: '0.5rem 0' }}>
            <GoldmineBadge estado={v.estadoDisco} />
            <span className="muted" style={{ fontSize: '0.85rem' }}>
              {goldmine(v.estadoDisco).nombre}
            </span>
            <Link to="/ayuda/goldmine" className="link-neon" style={{ fontSize: '0.78rem' }}>
              ¿qué significa?
            </Link>
            {v.estado !== 'DISPONIBLE' && (
              <span className={`chip`} style={{ borderColor: 'var(--neon-blue)' }}>{v.estado}</span>
            )}
          </div>

          <div className="ficha__price">{precio(v.precio)}</div>
          {v.descuentoCortePct > 0 && (
            <div className="corte-tag">✂ {v.descuentoCortePct}% de descuento en tu corte</div>
          )}

          <ul className="ficha__meta">
            {v.genero && <li><span>Género</span><span>{v.genero.nombre}</span></li>}
            {v.anio && <li><span>Año</span><span>{v.anio}</span></li>}
            {v.sello && <li><span>Sello</span><span>{v.sello}</span></li>}
            {v.edicionPais && <li><span>Edición</span><span>{v.edicionPais}</span></li>}
            <li><span>Formato</span><span>{v.formato}</span></li>
            <li><span>Modalidad</span><span>{v.senable ? 'Seña (50%)' : 'Compra directa (100%)'}</span></li>
          </ul>

          {v.descripcion && <p>{v.descripcion}</p>}

          <div style={{ marginTop: '1.25rem' }}>
            <button className="btn btn--neon btn--block" disabled title="Disponible en el próximo milestone (pagos)">
              {v.senable ? 'Señar' : 'Comprar'} — disponible pronto
            </button>
            {!disponible && (
              <p className="muted" style={{ fontSize: '0.82rem', marginTop: '0.5rem' }}>
                Este vinilo está {v.estado.toLowerCase()}.
              </p>
            )}
            {!usuario && (
              <p className="muted" style={{ fontSize: '0.82rem', marginTop: '0.5rem' }}>
                <Link to="/login" className="link-neon">Iniciá sesión</Link> para operar cuando estén habilitados los pagos.
              </p>
            )}
          </div>
        </div>
      </div>
    </section>
  )
}

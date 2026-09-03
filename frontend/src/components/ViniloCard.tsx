import { Link } from 'react-router-dom'
import type { ViniloResumen } from '../lib/types'
import { precio } from '../lib/format'
import { GoldmineBadge } from './GoldmineBadge'

/** Tarjeta del catalogo: portada, badge de estado y datos basicos. */
export function ViniloCard({ v }: { v: ViniloResumen }) {
  return (
    <Link to={`/catalogo/${v.id}`} className="vinilo-card">
      <div className="vinilo-card__cover">
        {v.portadaUrl ? (
          <img src={v.portadaUrl} alt={`${v.titulo} — ${v.artista}`} loading="lazy" />
        ) : null}
        {estadoBadge(v.estado)}
      </div>
      <div className="vinilo-card__body">
        <span className="vinilo-card__title">{v.titulo}</span>
        <span className="vinilo-card__artist">{v.artista}</span>
        <div className="vinilo-card__meta">
          <span className="price">{precio(v.precio)}</span>
          <GoldmineBadge estado={v.estadoDisco} />
        </div>
      </div>
    </Link>
  )
}

function estadoBadge(estado: string) {
  if (estado === 'VENDIDO') return <span className="badge badge--vendido">Vendido</span>
  return null
}

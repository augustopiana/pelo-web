import { goldmine } from '../lib/goldmine'

/**
 * Chip con la sigla Goldmine y un tooltip que explica el estado (spec §8).
 * Sin links internos: se usa dentro de tarjetas que ya son <a> (evita anchors anidados).
 * El acceso al glosario completo está en el footer y en la ficha.
 */
export function GoldmineBadge({ estado }: { estado: string }) {
  const info = goldmine(estado)
  return (
    <span className="gm">
      <span className="chip" tabIndex={0} aria-label={`Estado del disco: ${info.nombre}`}>
        {info.sigla}
      </span>
      <span className="gm__pop" role="tooltip">
        <strong>{info.nombre} ({info.sigla})</strong>
        <br />
        {info.descripcion}
      </span>
    </span>
  )
}

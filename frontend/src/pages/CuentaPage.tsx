import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { accountApi } from '../lib/api'
import type { Cupon, Orden } from '../lib/types'
import { precio } from '../lib/format'

export function CuentaPage() {
  const { usuario } = useAuth()
  const [ordenes, setOrdenes] = useState<Orden[]>([])
  const [cupones, setCupones] = useState<Cupon[]>([])

  useEffect(() => {
    accountApi.ordenes().then(setOrdenes).catch(() => {})
    accountApi.cupones().then(setCupones).catch(() => {})
  }, [])

  if (!usuario) return null

  return (
    <section>
      <h1>Mi cuenta</h1>

      {!usuario.emailVerificado && (
        <div className="alert alert--warn">
          Tu email todavía no está verificado. Revisá el link que te enviamos para poder señar o comprar.
        </div>
      )}

      <div className="card" style={{ maxWidth: 520, marginBottom: '1.5rem' }}>
        <ul className="ficha__meta" style={{ margin: 0 }}>
          <li><span>Nombre</span><span>{usuario.nombre}</span></li>
          <li><span>Email</span><span>{usuario.email}</span></li>
          <li><span>Rol</span><span>{usuario.rol}</span></li>
          <li><span>Verificado</span><span>{usuario.emailVerificado ? 'Sí' : 'No'}</span></li>
        </ul>
      </div>

      <h2 className="upper">Mis órdenes</h2>
      {ordenes.length === 0 ? (
        <p className="muted">Todavía no tenés órdenes. Cuando compres un vinilo, aparecerán acá.</p>
      ) : (
        <ul className="ficha__meta">
          {ordenes.map((o) => (
            <li key={o.id}>
              <span>
                {o.estado} · {o.modoEntrega === 'RETIRO' ? 'Retiro' : 'Envío'}
                {o.codigoRetiro ? ` · código ${o.codigoRetiro}` : ''}
              </span>
              <span>{precio(o.total)}</span>
            </li>
          ))}
        </ul>
      )}

      <h2 className="upper" style={{ marginTop: '1.5rem' }}>Mis cupones</h2>
      {cupones.length === 0 ? (
        <p className="muted">Todavía no tenés cupones de descuento en corte.</p>
      ) : (
        <ul className="ficha__meta">
          {cupones.map((c) => (
            <li key={c.id}>
              <span>{c.porcentaje}% · {c.estado}</span>
              <span>vence {new Date(c.fechaVencimiento).toLocaleDateString('es-AR')}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

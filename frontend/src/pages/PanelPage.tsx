import { useAuth } from '../auth/AuthContext'

// Panel del dueno (spec §9). Placeholder; el ABM/dashboard llegan en M2+.
export function PanelPage() {
  const { usuario } = useAuth()
  return (
    <section>
      <h1>Panel del dueño</h1>
      <p className="muted">Hola {usuario?.nombre}. Acá vas a gestionar el catálogo, los retiros y el dashboard.</p>
      <div className="card" style={{ maxWidth: 560 }}>
        <p className="upper" style={{ color: 'var(--neon)' }}>Próximamente (Milestone 2+)</p>
        <ul className="muted">
          <li>Alta / edición de vinilos y fotos</li>
          <li>Pausar / reactivar publicaciones</li>
          <li>Retiros, resolución ítem por ítem y ventas walk-in</li>
          <li>Dashboard y reembolsos</li>
        </ul>
      </div>
    </section>
  )
}

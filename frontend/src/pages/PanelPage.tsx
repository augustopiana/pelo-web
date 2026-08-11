import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

// Home del panel del dueno (spec §9).
export function PanelPage() {
  const { usuario } = useAuth()
  return (
    <section>
      <h1>Panel del dueño</h1>
      <p className="muted">Hola {usuario?.nombre}. Gestioná tu catálogo desde acá.</p>

      <div className="panel-cards">
        <Link to="/panel/vinilos" className="panel-card">
          <strong>Vinilos</strong>
          <span className="muted">Cargar, editar, subir fotos, pausar/reactivar.</span>
        </Link>
        <Link to="/panel/generos" className="panel-card">
          <strong>Géneros</strong>
          <span className="muted">Administrar la lista de géneros.</span>
        </Link>
        <div className="panel-card panel-card--soon">
          <strong>Dashboard y retiros</strong>
          <span className="muted">Próximamente (Milestone 3+).</span>
        </div>
      </div>
    </section>
  )
}

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
          <span className="muted">Cargar, editar, fotos, pausar/reactivar y venta walk-in.</span>
        </Link>
        <Link to="/panel/ordenes" className="panel-card">
          <strong>Órdenes</strong>
          <span className="muted">Ver compras; despachar envíos.</span>
        </Link>
        <Link to="/panel/retiros" className="panel-card">
          <strong>Retiro</strong>
          <span className="muted">Ingresar código y confirmar entrega.</span>
        </Link>
        <Link to="/panel/generos" className="panel-card">
          <strong>Géneros</strong>
          <span className="muted">Administrar la lista de géneros.</span>
        </Link>
        <div className="panel-card panel-card--soon">
          <strong>Dashboard</strong>
          <span className="muted">Métricas — próximamente (M7).</span>
        </div>
      </div>
    </section>
  )
}

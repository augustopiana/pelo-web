import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { HealthStatus } from '../components/HealthStatus'

export function BaseLayout() {
  const { usuario, logout } = useAuth()
  const navigate = useNavigate()

  const salir = () => {
    logout()
    navigate('/catalogo')
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-header__inner">
          <Link to="/catalogo" className="brand" aria-label="pelo discos">
            <span className="brand__pelo">pelo</span>
            <span className="brand__discos">discos</span>
          </Link>
          <nav className="app-nav">
            <NavLink to="/catalogo">Catálogo</NavLink>
            {usuario?.rol === 'ADMIN' && <NavLink to="/panel">Panel</NavLink>}
            {usuario ? (
              <>
                <NavLink to="/cuenta">Mi cuenta</NavLink>
                <a href="#" onClick={(e) => { e.preventDefault(); salir() }}>Salir</a>
              </>
            ) : (
              <>
                <NavLink to="/login">Ingresar</NavLink>
                <NavLink to="/registro">Crear cuenta</NavLink>
              </>
            )}
          </nav>
        </div>
      </header>

      <main className="app-main">
        <Outlet />
      </main>

      <footer className="app-footer">
        <div className="app-footer__inner">
          <span className="upper">pelo discos · vinilos de colección</span>
          <Link to="/ayuda/goldmine" className="upper">Escala Goldmine</Link>
          <HealthStatus />
        </div>
      </footer>
    </div>
  )
}

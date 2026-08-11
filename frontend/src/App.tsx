import { createBrowserRouter, Navigate } from 'react-router-dom'
import { BaseLayout } from './layout/BaseLayout'
import { CatalogoPage } from './pages/CatalogoPage'
import { FichaPage } from './pages/FichaPage'
import { CuentaPage } from './pages/CuentaPage'
import { PanelPage } from './pages/PanelPage'
import { PanelVinilosPage } from './pages/panel/PanelVinilosPage'
import { PanelViniloFormPage } from './pages/panel/PanelViniloFormPage'
import { PanelGenerosPage } from './pages/panel/PanelGenerosPage'
import { LoginPage } from './pages/LoginPage'
import { RegistroPage } from './pages/RegistroPage'
import { VerificarPage } from './pages/VerificarPage'
import { GlosarioPage } from './pages/GlosarioPage'
import { ProtectedRoute } from './auth/ProtectedRoute'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <BaseLayout />,
    children: [
      { index: true, element: <Navigate to="/catalogo" replace /> },
      { path: 'catalogo', element: <CatalogoPage /> },
      { path: 'catalogo/:id', element: <FichaPage /> },
      { path: 'ayuda/goldmine', element: <GlosarioPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'registro', element: <RegistroPage /> },
      { path: 'verificar', element: <VerificarPage /> },
      {
        path: 'cuenta',
        element: (
          <ProtectedRoute>
            <CuentaPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'panel',
        element: (
          <ProtectedRoute adminOnly>
            <PanelPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'panel/vinilos',
        element: (
          <ProtectedRoute adminOnly>
            <PanelVinilosPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'panel/vinilos/nuevo',
        element: (
          <ProtectedRoute adminOnly>
            <PanelViniloFormPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'panel/vinilos/:id',
        element: (
          <ProtectedRoute adminOnly>
            <PanelViniloFormPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'panel/generos',
        element: (
          <ProtectedRoute adminOnly>
            <PanelGenerosPage />
          </ProtectedRoute>
        ),
      },
    ],
  },
])

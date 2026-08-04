# frontend — vinilos (React + Vite + TypeScript)

SPA del módulo de vinilos. React 18 · Vite 5 · React Router 6.

## Requisitos
- Node 18+ (probado con Node 22).

## Configuración
- La URL del backend viene por variable de entorno `VITE_API_URL`.
- `.env.development` ya apunta a `http://localhost:8080`. Para overrides locales, copiar
  `.env.example` a `.env.local` (no versionado).

## Correr en local
```bash
npm install
npm run dev        # http://localhost:5173
```
El backend debe estar corriendo para que el health-check del pie de página muestre `Backend UP · BD UP`.

## Build
```bash
npm run build      # type-check + build de producción a dist/
```

## Estructura
```
src/
├── main.tsx                 # entrypoint + AuthProvider + RouterProvider
├── App.tsx                  # definición de rutas
├── auth/                    # AuthContext (tokens + auto-refresh), ProtectedRoute
├── layout/BaseLayout.tsx    # chrome (nav según sesión/rol)
├── pages/                   # Catalogo, Ficha, Login, Registro, Verificar, Cuenta, Panel, Glosario
├── components/              # ViniloCard, GoldmineBadge, HealthStatus
├── lib/                     # api.ts (cliente + auth), types.ts, goldmine.ts, format.ts
└── styles.css               # design system (chrome negro + catálogo blanco + rosa neón)
```

## Rutas
- `/catalogo` — catálogo público con búsqueda y filtros (spec §8).
- `/catalogo/:id` — ficha del vinilo (galería, datos, Goldmine, CTA).
- `/ayuda/goldmine` — glosario de la escala Goldmine.
- `/login`, `/registro`, `/verificar` — autenticación.
- `/cuenta` — cuenta del cliente (protegida).
- `/panel` — panel del dueño (protegida, solo ADMIN).

# pelo-web

Sistema para una peluquería que además vende vinilos. El desarrollo arranca por el **módulo de venta de vinilos** (catálogo, seña/compra online con Mercado Pago, retiro en el local) y luego sumará el módulo de turnos.

Se construye con **Spec Driven Development (SDD)**: primero la especificación del dominio, después la implementación contra criterios de aceptación.

## Estado
Fase de especificación / setup inicial. Ver `docs/registro-releases.md` para el punto exacto de avance.

## Stack
- **Backend:** Java + Spring Boot, PostgreSQL, Flyway.
- **Frontend:** React (Vite + TypeScript).
- **Pagos:** Mercado Pago.

## Mapa de documentos (`docs/`)
| Documento | Rol |
|-----------|-----|
| `spec-modulo-vinilos.md` | **Dominio / página web** (el "qué"). Fuente de verdad del comportamiento. |
| `documentacion-tecnica.md` | Diseño técnico (el "cómo funciona por detrás"). |
| `plan-de-desarrollo.md` | Roadmap por milestones. |
| `historias-usuario.md` | Backlog de historias y tasks, con seguimiento por checkboxes. |
| `registro-releases.md` | Bitácora por iteración: qué se hizo y desde dónde seguir. |

> **Separación importante:** la spec de dominio describe el negocio y la web. Las notas de desarrollo (técnico, plan, backlog, releases) son para el desarrollador y no deben mezclarse con el dominio.

## Cómo se trabaja
1. La spec de dominio define las reglas (R-1…R-13) y flujos.
2. El backlog traduce eso a historias con criterios de aceptación.
3. Se implementa por milestones (ver plan de desarrollo).
4. Al cerrar cada avance: marcar tasks en el backlog y anotar la entrada en el registro de releases (con el "próximo paso").

## Estructura del repositorio
```
pelo-web/
├── README.md
├── CLAUDE.md                # contexto permanente para Claude Code (auto-cargado)
├── .gitignore
├── .env.example             # variables de entorno (copiar a .env, no versionado)
├── docker-compose.yml       # PostgreSQL local para desarrollo
├── docs/                     # especificación y notas de desarrollo
│   ├── spec-modulo-vinilos.md
│   ├── documentacion-tecnica.md
│   ├── plan-de-desarrollo.md
│   ├── historias-usuario.md
│   ├── registro-releases.md
│   └── prompt-claude-code.md
├── backend/                  # Spring Boot (Java 21, Maven wrapper, Flyway)
└── frontend/                 # React + Vite + TypeScript
```

## Correr en local (setup del Milestone 0)
Requiere Docker (para PostgreSQL), JDK 21+ y Node 18+.
```bash
# 1) Base de datos
docker compose up -d

# 2) Backend (aplica la migración Flyway y expone /api/health)
cd backend && ./mvnw spring-boot:run        # en Windows: mvnw.cmd spring-boot:run

# 3) Frontend (http://localhost:5173)
cd frontend && npm install && npm run dev
```
El pie de página del frontend muestra el estado del circuito front → back → BD.
Detalle en `backend/README.md` y `frontend/README.md`.

## Trabajar con Claude Code
Lanzá Claude Code desde la raíz `pelo-web/`: carga `CLAUDE.md` automáticamente (contexto permanente del proyecto). Para arrancar o continuar, usá los prompts de `docs/prompt-claude-code.md`.

## Próximo paso
Milestone 0 — setup del monorepo y esqueletos (Spring Boot + React + PostgreSQL/Flyway con la migración inicial de entidades). Detalle en `docs/plan-de-desarrollo.md`.

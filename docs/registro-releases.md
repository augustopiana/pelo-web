# Registro de releases — Módulo de Vinilos

> **Notas de desarrollo.** Bitácora de cada iteración/entrega. Sirve para que, al retomar el proyecto, se sepa **qué está hecho y desde dónde seguir**. No mezclar con la spec de dominio.

## Cómo usar este registro
- Se usa **SemVer** (`MAJOR.MINOR.PATCH`):
  - **MAJOR:** cambios incompatibles de dominio/contrato.
  - **MINOR:** funcionalidad nueva compatible.
  - **PATCH:** correcciones.
- Por cada iteración se agrega una entrada **arriba de todo** (más nueva primero) con el formato de abajo.
- Registrar siempre: qué se agregó/cambió/arregló, **migraciones de BD** incluidas, decisiones tomadas y el punto exacto de continuación ("Próximo paso").
- Al cerrar una iteración, cruzar con `historias-usuario.md` y marcar las historias/tasks completadas.

---

## Plantilla de entrada

```
## [X.Y.Z] — AAAA-MM-DD — Título corto de la iteración

### Agregado
- …

### Cambiado
- …

### Arreglado
- …

### Migraciones / BD
- Vn__descripcion.sql

### Historias completadas
- HU-XX, HU-YY (ver historias-usuario.md)

### Decisiones
- …

### Próximo paso (desde dónde seguir)
- …
```

---

## Historial

## [0.4.0] — 2026-08-04 — Milestone 2: gestión de vinilos del dueño

### Agregado
- **Object storage (MinIO)** para las fotos: servicio `minio` + init de bucket (`pelo-fotos`, lectura pública) en `docker-compose`. Backend con SDK de S3 (`software.amazon.awssdk:s3`) detrás de una interfaz `StorageService` (S3-compatible; se cambia a S3/R2 real en prod sin tocar la lógica).
- **ABM de vinilos (E2)** — endpoints admin: `POST /vinilos` (alta), `PUT /vinilos/{id}` (edición), `PATCH /vinilos/{id}/pausar` (pausar/reactivar), `POST /vinilos/{id}/fotos` (subida multipart → MinIO; **primera foto = portada**), `DELETE /vinilos/{id}/fotos/{fotoId}`, `GET /admin/vinilos` (lista completa, incluye pausados/vendidos), `GET /admin/vinilos/{id}`.
- **Géneros (HU-06)**: `POST /generos` (alta), con `GET /generos` público ya existente.
- **Autorización admin**: escritura de vinilos/fotos/géneros exige `ROLE_ADMIN`; catálogo (GET) sigue público.
- **Panel del dueño (frontend)**: home con accesos, lista de vinilos con pausar/reactivar y editar, formulario de alta/edición con subida y borrado de fotos (preview + portada), y ABM de géneros. Rutas protegidas solo ADMIN.

### Cambiado
- `FotoDTO` ahora incluye el `id` (para poder borrar fotos desde el panel).
- Límite de subida multipart configurado (10 MB por archivo).

### Arreglado
- **401 vs 403**: un usuario autenticado sin permiso recibía 401 (por `sendError`, que re-dispatchaba a `/error` como anónimo). Se usa `setStatus(403)` en el `accessDeniedHandler`. Así el cliente autenticado obtiene 403 (no dispara refresh/logout) y el anónimo sigue en 401.

### Migraciones / BD
- Ninguna nueva. Las entidades `Vinilo`, `FotoVinilo`, `Genero` ya existían (M0).

### Historias completadas
- **HU-04** (alta con fotos), **HU-05** (editar / pausar / reactivar), **HU-06** (géneros). Épica **E2** cerrada. Ver `historias-usuario.md`.

### Decisiones
- Storage de fotos: **MinIO** (S3-compatible) en dev; en prod se apunta a S3/R2/Spaces o MinIO self-hosted sin cambiar código. Ver la nota de object storage guardada en el vault.

### Criterios de salida (M2)
- [x] El dueño publica un vinilo con fotos y aparece en el catálogo.
- [x] Pausar lo saca del catálogo; reactivar lo devuelve.

### Verificación
- E2E: alta por el panel → foto a MinIO (portada) → aparece en el catálogo público con portada; pausar oculta (catálogo 0), reactivar reaparece; autorización admin 200 / cliente 403 / anon 401.

### Próximo paso (desde dónde seguir)
- **Milestone 3 — Pagos: seña y compra directa (`v0.5.0`)**: órdenes (seña 50% / compra 100%), integración Mercado Pago en sandbox, confirmación por **webhook** idempotente, generación de **código de retiro**, **bloqueo de concurrencia** (pieza única, R-11) y cancelación con refund. Épicas E4 y E5.

## [0.3.1] — 2026-08-04 — Activación del login con Google

### Agregado
- **Login con Google operativo** end-to-end: integración de **Google Identity Services** en el frontend (`GoogleSignInButton`, script GIS en `index.html`, gated por `VITE_GOOGLE_CLIENT_ID`) contra el endpoint `POST /auth/google` que ya verificaba el ID token. Se cargó `GOOGLE_CLIENT_ID` en el backend y `VITE_GOOGLE_CLIENT_ID` en el frontend (`.env.local`, no versionado).

### Arreglado
- **Precedencia de env de Vite:** `.env.development` tenía prioridad sobre `.env.local` en modo dev, y su `VITE_GOOGLE_CLIENT_ID=` (vacío) pisaba el valor real. Se quitó esa clave de `.env.development` (queda solo en `.env.local`).
- **Artefactos de build:** `tsc -b` emitía `vite.config.js`/`.d.ts` y `*.tsbuildinfo` en el árbol; se redirigen a `node_modules/.tmp` y se ignora `*.tsbuildinfo`.

### Cierra
- Criterio de M1 "iniciar sesión (ambos métodos)": ahora **completo** (email/contraseña + Google). T-06 hecho.

### Verificación
- Backend: `POST /auth/google` pasó de 503 (no configurado) a 401 (token inválido) → toma el Client ID y verifica de verdad.
- Frontend: el botón "Sign in with Google" (GIS) renderiza sin errores de origen. El login real con la cuenta de Google lo completa el usuario.

### Próximo paso
- **Milestone 2 — Gestión de vinilos del dueño (`v0.4.0`)**: ABM de vinilos (`senable`, `descuento_corte_pct`), subida de fotos (portada = primera), gestión de géneros, pausar/reactivar (E2).

## [0.3.0] — 2026-08-04 — Milestone 1: cuentas y catálogo de lectura

### Agregado
- **Autenticación (E1):** Spring Security stateless con **JWT** (access + refresh). Endpoints `POST /auth/register`, `GET /auth/verify`, `POST /auth/login`, `POST /auth/refresh`, `GET /auth/me`, `POST /auth/google`. Contraseñas con **BCrypt**. `AuthUser` como principal; sin autenticación válida → 401.
- **Verificación de email (R-12):** token JWT firmado en el link; `EmailSender` con impl **dev** (loguea el link) y **SMTP** (prod, `app.mail.mode`).
- **Login con Google:** **scaffold** — `POST /auth/google` verifica el ID token (google-api-client), gated por `GOOGLE_CLIENT_ID` (deshabilitado hasta cargar credenciales).
- **Catálogo de lectura (E3):** `GET /vinilos` (búsqueda por texto, filtros artista/género/precio/estado, orden por más nuevos, paginación, **filtro de visibilidad R-9**), `GET /vinilos/{id}` (ficha con galería), `GET /generos`. `GET /ordenes/mias` y `/cupones/mios` autenticados (vacíos).
- **Seeder de dev** (`@Profile("dev")`): 7 géneros, 12 vinilos (2 ocultos a propósito por R-9), y 2 cuentas de prueba (`admin@pelo-web.local`/`admin1234`, `cliente@pelo-web.local`/`cliente1234`).
- **Frontend (React):** contexto de auth (tokens en localStorage + auto-refresh en 401), rutas protegidas y por rol. Páginas: catálogo (grilla, filtros, paginación), ficha (galería + descuento en corte + CTA), login, registro, verificación, mi cuenta, panel (placeholder admin), y **glosario Goldmine** (§8) con tooltip en las tarjetas/ficha.
- **Diseño:** design system "pelo discos" — chrome negro + catálogo blanco, acento **rosa neón #FF2E9A** (estética Discogs/Animals Records), theme-aware.

### Cambiado
- `WebCorsConfig` (WebMvc) → `CorsConfig` (`CorsConfigurationSource`) integrado con Spring Security.
- Excluido `UserDetailsServiceAutoConfiguration` (no se usa; evita el password autogenerado).

### Migraciones / BD
- Ninguna nueva. El esquema de M0 (`V1__init.sql`) ya cubre `usuario` (auth stateless, sin tabla de tokens) y `vinilo`/`foto_vinilo`/`genero` (catálogo).

### Historias completadas
- **HU-01** (registro + verificación), **HU-03** (mi cuenta), **HU-07/08/09** (catálogo: ver, buscar/filtrar, ficha + Goldmine). **HU-02** parcial: login email/contraseña OK; **Google pendiente** de credenciales (T-06). Ver `historias-usuario.md`.

### Decisiones
- Email en dev: **mailer que loguea el link** (SMTP real por env para prod).
- Login con Google: **scaffold + activar con credenciales después** (email/contraseña completo ahora).
- Datos de prueba: **seeder de perfil dev**.
- Tokens **stateless** (sin persistencia): verificación de email y refresh como JWT firmados.
- Diseño: chrome negro + catálogo blanco + rosa neón (elección del dueño).

### Criterios de salida (M1)
- [~] Registrarse, verificar email e iniciar sesión (ambos métodos) — email/contraseña **completo y verificado**; **Google scaffolded** (falta `GOOGLE_CLIENT_ID`).
- [x] El catálogo se navega, busca y filtra sobre datos de prueba.
- [x] Se explica la escala Goldmine en la web.

### Próximo paso (desde dónde seguir)
- **Cerrar el 10% de M1**: activar login con Google (crear credenciales OAuth en Google Cloud y cargar `GOOGLE_CLIENT_ID`).
- **Milestone 2 — Gestión de vinilos del dueño (`v0.4.0`)**: ABM de vinilos con `senable` y `descuento_corte_pct`, subida de fotos (portada = primera), gestión de géneros, pausar/reactivar (E2). Ver `docs/plan-de-desarrollo.md`.

## [0.2.0] — 2026-08-03 — Milestone 0: setup del monorepo y esqueletos

### Agregado
- **Backend Spring Boot** (`backend/`): Java 21, Spring Boot 3.4, Maven con **wrapper** (`mvnw`/`mvnw.cmd`). Dependencias: web, data-jpa, validation, actuator, flyway (core + postgresql), driver postgres.
- **Entidades JPA** del §4 de la spec (`domain/`): `Usuario`, `Genero`, `Vinilo`, `FotoVinilo`, `Orden`, `ItemOrden`, `Pago`, `Reembolso`, `Cupon`, `NotificacionDueno`, con todos los enums del punto 3.2 de la doc técnica (`@Enumerated(STRING)`). `Vinilo` incluye `@Version` para bloqueo optimista (R-11).
- **Perfiles `dev`/`prod`** (`application.yml` + `application-{dev,prod}.yml`); **secrets por variables de entorno**, sin credenciales en el repo. `ddl-auto: validate` (el esquema lo maneja Flyway).
- **Health-check E2E**: `GET /api/health` en el backend (estado + chequeo de BD vía `SELECT 1`), consumido desde el frontend (pie de página, muestra `Backend UP · BD UP`). CORS configurado para el dev server.
- **Frontend React + Vite + TypeScript** (`frontend/`): React Router con las **tres áreas** (Catálogo, Mi cuenta, Panel del dueño) como páginas placeholder, layout base, `lib/api.ts` (URL por `VITE_API_URL`), `.env.development` / `.env.example`.
- **Infra**: `docker-compose.yml` (PostgreSQL 16 —imagen `postgres:16-alpine`— con healthcheck) y `.env.example` en la raíz. READMEs de `backend/`, `frontend/` y raíz actualizados con instrucciones de "cómo correr".

### Migraciones / BD
- `V1__init.sql`: crea las 10 tablas del modelo de dominio (`usuario`, `genero`, `vinilo`, `foto_vinilo`, `orden`, `item_orden`, `pago`, `reembolso`, `cupon`, `notificacion_dueno`), con PK UUID, FKs, `UNIQUE` (email, código de retiro, nombre de género), enums como `varchar` + `CHECK`, columna `version` para bloqueo optimista e índices por estado/fechas/FKs.

### Historias completadas
- Ninguna historia HU se cierra en M0 (es setup de infraestructura). Las tasks T-10, T-22, T-43 y T-48 quedan **sin marcar**: solo se implementó su parte de entidades/migración (anotado inline en `historias-usuario.md`); sus endpoints/UI/lógica de negocio siguen pendientes.

### Decisiones
- Build backend: **Maven** (+ wrapper), por simplicidad y por ser el default del ecosistema Spring.
- Versión de Java fijada: **Java 21 LTS** (compila con el JDK 23 local vía release target 21).
- **Docker Compose** con Postgres 16 para levantar la BD en local. Se usa la imagen `postgres:16-alpine` (más liviana; el mismo Postgres 16 que en prod a nivel SQL).
- Spring Boot **3.4.x** (compatible con Java 21). Mercado Pago (Checkout Pro vs Payment Brick) sigue pendiente para el milestone de pagos (M3), no aplica a M0.

### Criterios de salida (M0)
- [x] `docker compose` levanta back + BD en local (Postgres 16 vía `docker-compose.yml`; backend con `./mvnw spring-boot:run`).
- [x] Migración inicial aplicada; esquema coincide con el modelo de dominio (Flyway `V1` + Hibernate `validate`).
- [x] Front renderiza y consume un endpoint del back (`/api/health` en el pie de página).

### Próximo paso (desde dónde seguir)
- **Milestone 1 — Cuentas y catálogo de lectura (`v0.3.0`)**: épicas E1 (registro + verificación de email, login email/contraseña y Google, JWT) y E3 (catálogo público: grilla, buscador, filtros, orden por más nuevos, ficha con glosario Goldmine). Ver `docs/plan-de-desarrollo.md`.

## [0.1.0] — 2026-08-03 — Documentación inicial (fase de especificación)

### Agregado
- Estructura del repositorio (`README.md`, `CLAUDE.md`, `.gitignore`, `docs/`, carpetas `backend/` y `frontend/` como placeholders).
- `CLAUDE.md` con el contexto permanente del proyecto para Claude Code.
- Spec de dominio del módulo de vinilos (`docs/spec-modulo-vinilos.md`).
- Documentación técnica inicial con stack definido: Java + Spring Boot (backend), React (frontend), PostgreSQL (`docs/documentacion-tecnica.md`).
- Plan de desarrollo por milestones (`docs/plan-de-desarrollo.md`).
- Backlog de historias de usuario y tasks (`docs/historias-usuario.md`).
- Prompts de arranque para Claude Code (`docs/prompt-claude-code.md`).
- Este registro de releases.

### Decisiones
- D-1: escala de estado del disco = **Goldmine** (con explicación de siglas en la web).
- D-2: stack = **Java + Spring Boot + React**.
- Separación de documentos: dominio (spec) vs. notas de desarrollo (técnico, releases, backlog).

### Pendientes / decisiones abiertas
- D-3: recordatorio de vencimiento de reserva al cliente (incluir o no en el MVP).
- Checkout Pro vs. Payment Brick de Mercado Pago (recomendado: Checkout Pro para empezar).
- Hosting / proveedor de object storage para fotos.

### Próximo paso (desde dónde seguir)
- Inicializar el repositorio: esqueleto Spring Boot + esquema inicial (Flyway) mapeando las entidades del §4 de la spec, y esqueleto React con routing y auth.
- Empezar por la épica de **Cuentas y autenticación** y el **catálogo público de solo lectura** (ver `historias-usuario.md`).

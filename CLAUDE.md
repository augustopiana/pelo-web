# CLAUDE.md — pelo-web

Contexto permanente del proyecto para Claude Code. Se carga automáticamente en cada sesión.

## Qué es esto
Sistema para una peluquería que además vende vinilos. Se desarrolla con **Spec Driven Development (SDD)**. Se arranca por el **módulo de venta de vinilos** (catálogo, **compra directa** online con Mercado Pago, entrega por **retiro en el local o envío por correo**, y **pedidos de búsqueda** de discos que no están). El módulo de turnos viene después.

> **Cambio de alcance (spec v0.2):** se eliminó la **seña/reserva**. Todo es compra directa. Ver `docs/spec-modulo-vinilos.md`.

## Stack
- **Backend:** Java + Spring Boot, PostgreSQL, Flyway (migraciones).
- **Frontend:** React + Vite + TypeScript.
- **Pagos:** Mercado Pago.
- Estructura monorepo: `backend/`, `frontend/`, `docs/`.

## Regla de oro (SDD)
`docs/spec-modulo-vinilos.md` es la **fuente de verdad** del comportamiento. Si el código y la spec entran en conflicto, manda la spec (o se actualiza la spec explícitamente antes de codear). No inventar reglas de negocio que no estén en la spec: si algo falta o es ambiguo, **preguntar** en vez de asumir.

## Mapa de documentos (leer bajo demanda, no todo de una)
- `docs/spec-modulo-vinilos.md` — dominio (**v0.2**): entidades (§4), máquinas de estado (§5), reglas R-6…R-15 (§6; R-1…R-5 y R-10 eliminadas), flujos (§7). Leer al implementar cualquier lógica de negocio.
- `docs/documentacion-tecnica.md` — arquitectura, entidades JPA, integración MP, jobs, endpoints. Leer al implementar.
- `docs/plan-de-desarrollo.md` — milestones y criterios de salida. Define **qué se hace ahora**.
- `docs/historias-usuario.md` — backlog con historias (HU-##) y tasks (T-##) con checkboxes.
- `docs/registro-releases.md` — bitácora por iteración y "próximo paso".

## Separación que se debe respetar
La spec de dominio describe el negocio y la web. Las notas técnicas (arquitectura, plan, backlog, releases) son de desarrollo. **No mezclar** detalle de implementación dentro de la spec de dominio.

## No-negociables (pitfalls fáciles de violar)
- **Pagos:** la confirmación de un pago se toma **del webhook de Mercado Pago**, nunca del redirect del navegador. Procesar webhooks de forma **idempotente**.
- **Pieza única:** cada vinilo tiene stock 1. Al comprar hay que **bloquear** para evitar doble venta (R-11). Verificar `DISPONIBLE` y cambiar estado atómicamente. Solo con el webhook aprobado el vinilo pasa a `VENDIDO`.
- **Cuenta verificada:** un cliente no puede comprar ni crear pedidos de búsqueda sin email verificado (R-12).
- **Compra directa (único modo):** venta final, pago 100% online (R-6). No hay seña ni reserva.
- **Entrega (R-14):** el cliente elige **retiro** (con código, R-13) o **envío** (se guardan datos de dirección; despacho manual del dueño; el pago online cubre solo el disco). Integración con el correo = futuro (D-7).
- **Pedidos de búsqueda (R-15):** solo clientes con cuenta verificada; notifican al dueño; al marcarlos `encontrado` se avisa al cliente por email.
- **Cupón:** 1 por compra, máximo 1 cada 30 días corridos, % = el mayor de los ítems de la orden, validez 2 meses (R-8).
- **Código de retiro:** aleatorio, único, legible, sin caracteres ambiguos (R-13). Solo en órdenes con `modoEntrega = RETIRO`.
- **Secrets:** credenciales de MP/SMTP/BD/JWT/MinIO nunca en el repo; van por variables de entorno.

## Flujo de trabajo por iteración
1. Mirar `docs/plan-de-desarrollo.md` y tomar el milestone activo.
2. Implementar contra los **criterios de aceptación** de las historias (`docs/historias-usuario.md`).
3. Al terminar: **marcar las tasks/historias** como hechas (`[x]`) en el backlog.
4. Agregar/actualizar la entrada en `docs/registro-releases.md` (qué se hizo, migraciones incluidas, y el **próximo paso**).
5. No cerrar un milestone sin cumplir sus criterios de salida.

## Estado actual
Milestones **0–2 completados** (`v0.2.0`–`v0.4.0`): setup del monorepo, cuentas+auth (incl. Google), catálogo público, y gestión de vinilos del dueño con fotos en **MinIO** (object storage S3-compatible). **Próximo paso: Milestone 3 — compra directa** (órdenes 100%, MP sandbox, webhook idempotente, concurrencia R-11, entrega retiro/envío). Ver `docs/plan-de-desarrollo.md` y `docs/registro-releases.md`.

## Setup ya definido
- Backend: **Maven** (con wrapper `mvnw`), Java 21, Spring Boot 3.4.
- **Docker Compose** para PostgreSQL 16 (alpine) y **MinIO** en local.
- Mercado Pago: **Checkout Pro** (sandbox) cuando se implemente el pago.

## Convenciones
- Commits: Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:`…).
- Versionado: SemVer, reflejado en el registro de releases.
- Cambios de esquema: siempre por migración Flyway, nunca a mano.

# CLAUDE.md — pelo-web

Contexto permanente del proyecto para Claude Code. Se carga automáticamente en cada sesión.

## Qué es esto
Sistema para una peluquería que además vende vinilos. Se desarrolla con **Spec Driven Development (SDD)**. Se arranca por el **módulo de venta de vinilos** (catálogo, seña/compra online con Mercado Pago, retiro en el local). El módulo de turnos viene después.

## Stack
- **Backend:** Java + Spring Boot, PostgreSQL, Flyway (migraciones).
- **Frontend:** React + Vite + TypeScript.
- **Pagos:** Mercado Pago.
- Estructura monorepo: `backend/`, `frontend/`, `docs/`.

## Regla de oro (SDD)
`docs/spec-modulo-vinilos.md` es la **fuente de verdad** del comportamiento. Si el código y la spec entran en conflicto, manda la spec (o se actualiza la spec explícitamente antes de codear). No inventar reglas de negocio que no estén en la spec: si algo falta o es ambiguo, **preguntar** en vez de asumir.

## Mapa de documentos (leer bajo demanda, no todo de una)
- `docs/spec-modulo-vinilos.md` — dominio: entidades (§4), máquinas de estado (§5), reglas R-1…R-13 (§6), flujos (§7). Leer al implementar cualquier lógica de negocio.
- `docs/documentacion-tecnica.md` — arquitectura, entidades JPA, integración MP, jobs, endpoints. Leer al implementar.
- `docs/plan-de-desarrollo.md` — milestones y criterios de salida. Define **qué se hace ahora**.
- `docs/historias-usuario.md` — backlog con historias (HU-##) y tasks (T-##) con checkboxes.
- `docs/registro-releases.md` — bitácora por iteración y "próximo paso".

## Separación que se debe respetar
La spec de dominio describe el negocio y la web. Las notas técnicas (arquitectura, plan, backlog, releases) son de desarrollo. **No mezclar** detalle de implementación dentro de la spec de dominio.

## No-negociables (pitfalls fáciles de violar)
- **Pagos:** la confirmación de un pago se toma **del webhook de Mercado Pago**, nunca del redirect del navegador. Procesar webhooks de forma **idempotente**.
- **Pieza única:** cada vinilo tiene stock 1. Al reservar/comprar hay que **bloquear** para evitar doble venta (R-11). Verificar `DISPONIBLE` y cambiar estado atómicamente.
- **Cuenta verificada:** un cliente no puede señar/comprar sin email verificado (R-12).
- **Quién marca qué:** el **rechazo al probar** lo marca el **dueño** en el panel, en persona (R-3). La **cancelación antes de probar** la marca el **cliente** (R-4). No mezclar.
- **Órdenes:** una orden no mezcla vinilos señables y no señables (R-10). Señable → solo seña (50%). No señable → solo compra directa (100%).
- **Cupón:** 1 por compra, máximo 1 cada 30 días corridos, % = el mayor de los ítems vendidos, validez 2 meses (R-8).
- **Código de retiro:** aleatorio, único, legible, sin caracteres ambiguos (R-13).
- **Secrets:** credenciales de MP/SMTP/BD/JWT nunca en el repo; van por variables de entorno.

## Flujo de trabajo por iteración
1. Mirar `docs/plan-de-desarrollo.md` y tomar el milestone activo.
2. Implementar contra los **criterios de aceptación** de las historias (`docs/historias-usuario.md`).
3. Al terminar: **marcar las tasks/historias** como hechas (`[x]`) en el backlog.
4. Agregar/actualizar la entrada en `docs/registro-releases.md` (qué se hizo, migraciones incluidas, y el **próximo paso**).
5. No cerrar un milestone sin cumplir sus criterios de salida.

## Estado actual
Fase de especificación completa. **Próximo paso: Milestone 0** (setup del monorepo + esqueletos + migración inicial de entidades). Ver `docs/plan-de-desarrollo.md`.

## Decisiones de setup pendientes (confirmar antes o al inicio del M0)
- Build del backend: **Maven o Gradle**.
- **Docker Compose** para levantar PostgreSQL en local: sí/no.
- Mercado Pago: empezar con **Checkout Pro** (recomendado) o Payment Brick.

## Convenciones
- Commits: Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:`…).
- Versionado: SemVer, reflejado en el registro de releases.
- Cambios de esquema: siempre por migración Flyway, nunca a mano.

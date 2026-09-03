# Plan de desarrollo — pelo-web (Módulo de Vinilos)

> **Notas de desarrollo.** Roadmap por milestones. Cada milestone agrupa épicas del backlog (`historias-usuario.md`), apunta a una versión del `registro-releases.md` y tiene **criterios de salida** claros. El objetivo es poder frenar y retomar sin perder el hilo.
> Regla de trabajo (SDD): la spec de dominio manda; las historias se implementan contra sus criterios de aceptación; cada milestone cerrado se anota en el registro de releases y se marcan las tasks hechas.

## Convenciones
- Una rama por historia o por milestone (a definir), merge cuando pasan los criterios de aceptación.
- "Definición de hecho" (DoD) de una historia: código + tests mínimos + tasks marcadas en `historias-usuario.md` + entrada/actualización en `registro-releases.md`.
- Mercado Pago se integra primero en **sandbox**; el paso a prod es parte del milestone final.

---

## Milestone 0 — Setup del repositorio y esqueletos → `v0.2.0`
**Meta:** repo funcionando de punta a punta con un "hola mundo" real (front habla con back, back habla con BD).

Alcance:
- Estructura del monorepo: `backend/` (Spring Boot), `frontend/` (React + Vite + TS), `docs/`.
- Backend: proyecto Spring Boot + PostgreSQL + Flyway; **migración inicial** con todas las entidades del §4 de la spec (aunque los endpoints vengan después).
- Frontend: esqueleto React con routing y layout base (catálogo, cuenta, panel).
- Configuración de perfiles (`dev`/`prod`) y manejo de secrets por variables de entorno.
- Health-check end-to-end (un endpoint simple consumido desde el front).

**Criterios de salida:**
- [x] `docker compose` o instrucciones levantan back + BD en local.
- [x] Migración inicial aplicada; esquema coincide con el modelo de dominio.
- [x] Front renderiza y consume un endpoint del back.

> Cerrado en `v0.2.0` (2026-08-03). Verificado: Flyway aplicó `V1__init.sql` (10 tablas del §4), Hibernate `validate` OK, `/api/health` responde `BD UP` y el front lo muestra. Ver `registro-releases.md`.

---

## Milestone 1 — Cuentas y catálogo de lectura → `v0.3.0`
**Meta:** un visitante navega el catálogo; un cliente se registra y verifica su cuenta.
**Épicas:** E1 (Cuentas y autenticación), E3 (Catálogo y descubrimiento).

Alcance: HU-01…HU-03, HU-07…HU-09.
- Registro + verificación por email, login email/contraseña, login Google, JWT.
- Catálogo público: grilla, buscador, filtros (artista/género/precio/estado), orden por más nuevos, ficha con galería y tooltip/glosario Goldmine.

**Criterios de salida:**
- [x] Un cliente puede registrarse, verificar email e iniciar sesión (ambos métodos). _(email/contraseña + **login con Google** activado)_
- [x] El catálogo se navega, busca y filtra sobre datos de prueba.
- [x] Se explica la escala Goldmine en la web.

> **Cerrado** en `v0.3.0` + `v0.3.1` (2026-08-04). Verificado E2E: registro→verificación (link en log dev)→login→/me→ruta protegida; catálogo con búsqueda/filtros/orden/ficha sobre datos sembrados; glosario Goldmine. Login con Google activado (GIS en el front + verificación de ID token en el back). Ver `registro-releases.md`.

---

## Milestone 2 — Gestión de vinilos (dueño) → `v0.4.0`
**Meta:** el dueño carga y administra su catálogo real.
**Épicas:** E2 (Publicación y gestión), base de E11 (panel).

Alcance: HU-04…HU-06.
- ABM de vinilos con `senable` y `descuento_corte_pct`, subida de fotos (portada = primera), gestión de géneros, pausar/reactivar.

**Criterios de salida:**
- [x] El dueño publica un vinilo con fotos y aparece en el catálogo.
- [x] Pausar lo saca del catálogo; reactivar lo devuelve.

> **Cerrado** en `v0.4.0` (2026-08-04). Verificado E2E: alta de vinilo por el panel → subida de fotos a MinIO (primera = portada) → aparece en el catálogo público con su portada; pausar lo oculta y reactivar lo devuelve. Fotos en object storage S3-compatible. Autorización de escritura solo ADMIN. Ver `registro-releases.md`.

---

> **Nota de alcance (spec v0.2):** se eliminó la **seña/reserva**. El único modo online es la **compra directa** (100%). Se agregó la **entrega** (retiro con código / envío por correo manual) y los **pedidos de búsqueda**. Los milestones de acá en adelante se replantearon en consecuencia.

## Milestone 3 — Compra directa (pago) → `v0.5.0`
**Meta:** el cliente paga online el 100% y el stock reacciona correctamente.
**Épicas:** E5 (Compra directa).

Alcance: HU-12 + elección de entrega en el checkout.
- Órdenes (100%), integración Mercado Pago en **sandbox**, confirmación por **webhook** (idempotente), **bloqueo de concurrencia** (pieza única, R-11), elección de **modo de entrega** (retiro / envío + datos de dirección) y generación de **código de retiro** (solo retiro, R-13).

**Criterios de salida:**
- [x] Comprar deja el vinilo `vendido` **solo tras el pago aprobado**; si es retiro, con código generado; si es envío, con los datos de envío guardados.
- [x] Dos usuarios no pueden comprar la misma pieza (R-11).
- [x] Un pago rechazado deja la orden `cancelada` y libera el vinilo.

> **Cerrado** en `v0.5.0` (2026-09-02) con **gateway de dev** (pago simulado + confirmación idempotente). Verificado E2E: crear orden (retiro/envío) → R-11 (409 en doble compra) → pago aprobado (vendido + código R-13) → idempotencia → rechazo (cancelada + liberado) → R-12 (403 sin verificar) → checkout en la UI. **Pendiente:** cablear **Mercado Pago real** (Checkout Pro sandbox) — se activa con credenciales sin tocar la lógica (T-34). Ver `registro-releases.md`.

---

## Milestone 4 — Entrega (retiro / envío) y walk-in → `v0.6.0`
**Meta:** cerrar el ciclo de entrega.
**Épicas:** E6 (Entrega), E7 (Venta walk-in).

Alcance: HU-13 (retiro/entrega), HU-14 (despacho de envío), HU-15 (walk-in).
- Ingreso de código y confirmación de entrega (retiro); marcar orden como despachada (envío); venta en efectivo walk-in sobre disponibles.

**Criterios de salida:**
- [ ] El dueño ingresa un código y confirma la entrega → orden `entregada`.
- [ ] El dueño ve los datos de envío y marca la orden `enviada`.
- [ ] La venta walk-in saca el vinilo del catálogo al instante.

---

## Milestone 5 — Pedidos de búsqueda → `v0.7.0`
**Meta:** que el cliente pueda pedir un disco que no está y el dueño lo gestione.
**Épicas:** E12 (Pedidos de búsqueda).

Alcance: HU-23, HU-24.
- El cliente (verificado) crea un pedido de búsqueda; el dueño recibe notificación; el cliente ve el estado; el dueño resuelve (encontrado/no encontrado) con aviso al cliente.

**Criterios de salida:**
- [ ] Un cliente crea un pedido y lo ve en estado `buscando`; el dueño recibe la notificación.
- [ ] Al marcar `encontrado`, el cliente recibe un email y ve el nuevo estado; el dueño ve su contacto.

---

## Milestone 6 — Cupones, jobs y notificaciones → `v0.8.0`
**Meta:** automatismos y avisos.
**Épicas:** E8 (Cupones), E9 (Jobs), E10 (Notificaciones).

Alcance: HU-16, HU-18, HU-19, HU-20.
- Generación de cupón (1 por compra, tope 1 cada 30 días, mayor %, validez 2 meses).
- Job: ocultamiento de vendidos a 30 días.
- Notificaciones: cartelito + email al dueño (nueva compra, nuevo pedido de búsqueda); emails al cliente (confirmación, vinilo encontrado).

**Criterios de salida:**
- [ ] El cupón se genera respetando el tope y el mayor % de los ítems de la compra.
- [ ] Un vendido con +30 días desaparece del catálogo (sin borrarse).

---

## Milestone 7 — Panel/dashboard, reembolsos y hardening → `v0.9.0`
**Meta:** dejar el panel usable y el sistema robusto.
**Épicas:** E11 (Panel y dashboard).

Alcance: HU-21, HU-22 + endurecimiento.
- Dashboard (ventas del mes, órdenes pendientes de entrega/despacho, pedidos de búsqueda abiertos), gestión de reembolsos (excepcionales), testing E2E de los flujos críticos, validaciones y manejo de errores.

**Criterios de salida:**
- [ ] Dashboard muestra métricas reales.
- [ ] Suite de tests cubre los flujos de la spec (R-6…R-15).

---

## v1.0.0 — MVP en producción
**Meta:** salir a la calle.
- Mercado Pago en **producción** (credenciales prod, webhook público por HTTPS).
- Hosting definido (backend siempre-activo por webhooks/jobs), object storage para fotos, dominio y certificados.
- Datos reales cargados por el dueño; comprobante interno operativo.

---

## Fases futuras (post-MVP, fuera de este plan)
- Módulo de turnos/peluquería y **redención de cupones**.
- **Integración con la API del correo (Correo Argentino / OCA)**: cotización, etiquetas y tracking (en el MVP el envío es manual). Ver D-7.
- Carga por IA (foto tapa/contratapa → autocompletado).
- WhatsApp automático al dueño.
- CDs y cassettes (campo `formato` ya preparado).
- Facturación electrónica (AFIP/ARCA).

---

## Dependencias y orden
```
M0 setup
 └─► M1 cuentas + catálogo lectura
      ├─► M2 gestión vinilos (dueño)
      ├─► M3 compra directa (pago)
      │    └─► M4 entrega (retiro/envío) + walk-in
      └─► M5 pedidos de búsqueda
           (M3/M4/M5 en paralelo salvo dependencias)
                └─► M6 cupones + jobs + notificaciones
                     └─► M7 panel + hardening
                          └─► v1.0.0 producción
```

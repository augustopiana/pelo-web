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
- [ ] El dueño publica un vinilo con fotos y aparece en el catálogo.
- [ ] Pausar lo saca del catálogo; reactivar lo devuelve.

---

## Milestone 3 — Pagos: seña y compra directa → `v0.5.0`
**Meta:** el cliente paga online y el stock reacciona correctamente.
**Épicas:** E4 (Seña), E5 (Compra directa).

Alcance: HU-10, HU-11, HU-12.
- Órdenes (seña 50% / compra 100%), integración Mercado Pago en **sandbox**, confirmación por **webhook** (idempotente), generación de **código de retiro**, **bloqueo de concurrencia** (pieza única), cancelación de reserva con refund.

**Criterios de salida:**
- [ ] Señar deja el vinilo `reservado` con vencimiento +7 días y código generado, solo tras webhook aprobado.
- [ ] Comprar directo (no señable) deja el vinilo `vendido`.
- [ ] Dos usuarios no pueden reservar la misma pieza.
- [ ] Cancelar antes de probar devuelve la seña.

---

## Milestone 4 — Retiro, resolución y venta walk-in → `v0.6.0`
**Meta:** cerrar el ciclo físico en el local.
**Épicas:** E6 (Retiro y resolución), E7 (Venta walk-in).

Alcance: HU-13, HU-14, HU-15.
- Ingreso de código, resolución **ítem por ítem** (vender cobrando el resto / rechazar con refund), confirmación de entrega de compra directa, venta en efectivo walk-in sobre disponibles.

**Criterios de salida:**
- [ ] El dueño resuelve una orden multi-vinilo vendiendo unos y rechazando otros, con refunds correctos.
- [ ] La venta walk-in saca el vinilo del catálogo al instante.

---

## Milestone 5 — Cupones, jobs y notificaciones → `v0.7.0`
**Meta:** automatismos y avisos.
**Épicas:** E8 (Cupones), E9 (Jobs), E10 (Notificaciones).

Alcance: HU-16, HU-17, HU-18, HU-19, HU-20.
- Generación de cupón (1 por compra, tope 1 cada 30 días, mayor %, validez 2 meses).
- Jobs: vencimiento de reservas (seña perdida) y ocultamiento de vendidos a 30 días.
- Notificaciones: cartelito + email al dueño; email de confirmación al cliente.

**Criterios de salida:**
- [ ] El cupón se genera respetando el tope y el mayor % de los ítems vendidos.
- [ ] Una reserva no retirada vence sola a los 7 días.
- [ ] Un vendido con +30 días desaparece del catálogo (sin borrarse).

---

## Milestone 6 — Panel/dashboard, reembolsos y hardening → `v0.8.0`
**Meta:** dejar el panel usable y el sistema robusto.
**Épicas:** E11 (Panel y dashboard).

Alcance: HU-21, HU-22 + endurecimiento.
- Dashboard (reservas activas con vencimiento, ventas del mes, señas pendientes de retiro), gestión de reembolsos, testing E2E de los flujos críticos, validaciones y manejo de errores.

**Criterios de salida:**
- [ ] Dashboard muestra métricas reales.
- [ ] Suite de tests cubre los flujos de la spec (R-1…R-13).

---

## v1.0.0 — MVP en producción
**Meta:** salir a la calle.
- Mercado Pago en **producción** (credenciales prod, webhook público por HTTPS).
- Hosting definido (backend siempre-activo por webhooks/jobs), object storage para fotos, dominio y certificados.
- Definir D-3 (recordatorio de vencimiento al cliente): incluir o no.
- Datos reales cargados por el dueño; comprobante interno operativo.

---

## Fases futuras (post-MVP, fuera de este plan)
- Módulo de turnos/peluquería y **redención de cupones**.
- Envío por correo.
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
      └─► M3 pagos (seña + compra)
           └─► M4 retiro + walk-in
                └─► M5 cupones + jobs + notificaciones
                     └─► M6 panel + hardening
                          └─► v1.0.0 producción
```

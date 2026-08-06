# Backlog — Historias de usuario y tasks (Módulo de Vinilos)

> **Notas de desarrollo.** Backlog derivado de `spec-modulo-vinilos.md`. Se marca el avance con checkboxes: `[ ]` pendiente, `[x]` hecho. Cada historia lista sus criterios de aceptación (CA) y sus tasks técnicas. Al completar, cruzar con `registro-releases.md`.
> Convención de IDs: `HU-##` historias, `T-##` tasks. Referencias a reglas (`R-#`) y flujos (`Flujo X`) apuntan a la spec de dominio.

## Estado global (resumen)
- [x] E1 — Cuentas y autenticación _(email/contraseña + verificación + JWT + login con Google + cuenta)_
- [ ] E2 — Publicación y gestión de vinilos (dueño)
- [x] E3 — Catálogo y descubrimiento (cliente)
- [ ] E4 — Seña
- [ ] E5 — Compra directa
- [ ] E6 — Retiro y resolución
- [ ] E7 — Venta walk-in
- [ ] E8 — Cupones
- [ ] E9 — Jobs automáticos
- [ ] E10 — Notificaciones
- [ ] E11 — Panel y dashboard del dueño

---

## E1 — Cuentas y autenticación

### HU-01 — Registro con email
Como visitante, quiero crear una cuenta con email y contraseña para poder operar.
- CA: la cuenta queda **no verificada** y no puede señar/comprar hasta verificar el email (**R-12**).
- CA: contraseña almacenada con hash (BCrypt).
- [x] T-01 Endpoint `POST /auth/register` + validaciones
- [x] T-02 Envío de email de verificación con token _(dev: mailer que loguea el link; SMTP real cableado para prod)_
- [x] T-03 Endpoint `GET /auth/verify`
- [x] T-04 UI de registro y estado "verificá tu email"

### HU-02 — Login (email y Google)
Como cliente, quiero iniciar sesión con email/contraseña o con Google.
- CA: login con Google vincula por `google_id`.
- CA: se emite JWT (access/refresh).
- [x] T-05 Login email/contraseña + JWT (access + refresh)
- [x] T-06 Google OAuth2 _(Google Identity Services en el front → `POST /auth/google` verifica el ID token; activado con `GOOGLE_CLIENT_ID`)_
- [x] T-07 UI de login con ambas opciones _(email/contraseña + botón "Sign in with Google")_

### HU-03 — Ver mi cuenta
Como cliente, quiero ver mis órdenes, sus estados, mis vinilos comprados y mis cupones.
- [x] T-08 Endpoints `/ordenes/mias`, `/cupones/mios` _(autenticados; vacíos hasta que existan órdenes/cupones en M3+)_
- [x] T-09 UI de "Mi cuenta"

---

## E2 — Publicación y gestión de vinilos (dueño)

### HU-04 — Alta de vinilo
Como dueño, quiero cargar un vinilo con todos sus datos y fotos.
- CA: define `senable` y `descuento_corte_pct`.
- CA: estado del disco en **escala Goldmine**.
- CA: la **primera foto** es la portada (Flujo/§8).
- [x] T-10 Entidades JPA `Vinilo`, `FotoVinilo`, `Genero` + migración _(migración/entidades en M0 · v0.2.0)_
- [ ] T-11 Endpoint `POST /vinilos` + subida de fotos a storage
- [ ] T-12 UI de alta en el panel

### HU-05 — Editar / pausar vinilo
Como dueño, quiero editar datos o pausar/reactivar una publicación.
- CA: `pausado` sale del catálogo público y puede volver a `disponible`.
- [ ] T-13 Endpoints `PUT /vinilos/{id}`, `PATCH /vinilos/{id}/pausar`
- [ ] T-14 UI de edición y pausa

### HU-06 — Gestionar géneros
Como dueño, quiero una lista base de géneros y poder agregar nuevos.
- [ ] T-15 CRUD mínimo de `Genero`

---

## E3 — Catálogo y descubrimiento (cliente)

### HU-07 — Ver catálogo
Como visitante, quiero ver los vinilos disponibles con su portada, precio y estado.
- CA: se muestran `reservado`/`vendido` hasta el ocultamiento a 30 días (**R-9**).
- [x] T-16 Endpoint `GET /vinilos` con paginación _(incluye filtro de visibilidad R-9)_
- [x] T-17 UI grilla de catálogo

### HU-08 — Buscar y filtrar
Como visitante, quiero buscar por texto y filtrar por artista, género, precio y estado, y ordenar por más nuevos.
- [x] T-18 Query con filtros + orden por `fecha_publicacion`
- [x] T-19 UI de buscador y filtros

### HU-09 — Ver ficha y entender la escala
Como visitante, quiero ver la ficha completa y entender qué significan las siglas de estado.
- CA: galería, datos, precio, **descuento en corte**, CTA Señar/Comprar según `senable`.
- CA: **explicación de la escala Goldmine** accesible (tooltip y/o glosario) (§8).
- [x] T-20 Endpoint `GET /vinilos/{id}`
- [x] T-21 UI de ficha + tooltip/glosario Goldmine

---

## E4 — Seña

### HU-10 — Señar uno o varios vinilos
Como cliente verificado, quiero señar vinilos señables pagando el 50%.
- CA: una orden **no mezcla** señables y no señables (**R-10**).
- CA: seña = **50%** del total, online por MP (**R-1**).
- CA: al aprobarse el pago (webhook), vinilos → `reservado`, se fija vencimiento +7 días y se genera **código de retiro** (**R-13**).
- CA: doble reserva evitada por bloqueo (**R-11**).
- [ ] T-22 Entidades `Orden`, `ItemOrden`, `Pago` + migración _(migración/entidades hechas en M0 · v0.2.0; endpoint pendiente)_
- [ ] T-23 Endpoint `POST /ordenes/sena`
- [ ] T-24 Integración MP: creación de pago (50%)
- [ ] T-25 Webhook MP + confirmación idempotente
- [ ] T-26 Generación de código de retiro
- [ ] T-27 Bloqueo de concurrencia (R-11)
- [ ] T-28 UI de selección y checkout de seña

### HU-11 — Cancelar mi reserva antes de probar
Como cliente, quiero cancelar mi reserva y recuperar la seña mientras esté vigente (**R-4**).
- CA: lo dispara el **cliente**; vinilos → `disponible`; reembolso por MP.
- [ ] T-29 Endpoint `POST /ordenes/{id}/cancelar`
- [ ] T-30 Refund MP + `Reembolso` (motivo cancelación)
- [ ] T-31 UI de cancelación

---

## E5 — Compra directa

### HU-12 — Comprar directo
Como cliente verificado, quiero comprar un vinilo no señable pagando el 100%.
- CA: solo para `senable = false`; venta **final sin prueba** (**R-6**).
- CA: al aprobarse el pago, vinilos → `vendido`, código de retiro y (si aplica) cupón.
- [ ] T-32 Endpoint `POST /ordenes/compra`
- [ ] T-33 Integración MP: pago 100%
- [ ] T-34 UI de compra directa

---

## E6 — Retiro y resolución

### HU-13 — Resolver retiro de seña por vinilo
Como dueño, quiero ingresar el código y resolver cada vinilo (vender o rechazar).
- CA: **el dueño** marca el rechazo, en persona (**R-3**); el cliente no puede.
- CA: "Confirmar venta" cobra el resto (efectivo u online) → `vendido`.
- CA: "Rechazado" → refund de la seña de ese ítem → vinilo `disponible`.
- CA: orden pasa a `cerrada` cuando no quedan ítems `pendiente`; se evalúa cupón (E8).
- [ ] T-35 Endpoint `POST /retiros/{codigo}` (buscar orden)
- [ ] T-36 Endpoint vender ítem (+ registrar pago del resto)
- [ ] T-37 Endpoint rechazar ítem (+ refund)
- [ ] T-38 UI de resolución ítem por ítem

### HU-14 — Confirmar entrega de compra directa
Como dueño, quiero ingresar el código y confirmar la entrega.
- [ ] T-39 Endpoint `POST /retiros/{codigo}/entregar`
- [ ] T-40 UI de entrega

---

## E7 — Venta walk-in

### HU-15 — Vender en efectivo en el local
Como dueño, quiero marcar un vinilo disponible como vendido en efectivo (**Flujo E**).
- CA: solo sobre `disponible` (**R-7**); sale del catálogo de inmediato.
- [ ] T-41 Endpoint `POST /vinilos/{id}/venta-efectivo`
- [ ] T-42 UI de venta walk-in

---

## E8 — Cupones

### HU-16 — Generar cupón de descuento en corte
Como cliente, al concretar una compra quiero recibir mi cupón de descuento en corte (**R-8**).
- CA: 1 cupón por compra; **máx 1 cada 30 días corridos** desde el último.
- CA: % = **mayor** de los ítems vendidos/confirmados de la orden.
- CA: validez **2 meses**; queda en la cuenta del cliente.
- [ ] T-43 Entidad `Cupon` + migración _(migración/entidad hecha en M0 · v0.2.0; lógica de generación pendiente)_
- [ ] T-44 Lógica event-driven al cerrar orden (tope 30 días + mayor %)
- [ ] T-45 UI de "Mis cupones"

---

## E9 — Jobs automáticos

### HU-17 — Vencer reservas
Como sistema, quiero vencer reservas no retiradas a los 7 días (**Flujo F**).
- CA: ítems `pendiente` → `vencido`, vinilos → `disponible`, orden → `vencida`, **seña perdida**.
- [ ] T-46 Job programado de vencimiento

### HU-18 — Ocultar vendidos
Como sistema, quiero ocultar del catálogo los vendidos con más de 30 días (**Flujo G**).
- CA: no se borra el registro; se filtra en el catálogo.
- [ ] T-47 Job programado de ocultamiento + filtro en queries

---

## E10 — Notificaciones

### HU-19 — Avisar al dueño
Como dueño, quiero enterarme de nuevas señas/compras por el panel y por email.
- CA: registro en `NotificacionDueno` (cartelito) + email. WhatsApp = fase 2.
- [ ] T-48 Entidad `NotificacionDueno` + badge en panel _(migración/entidad hecha en M0 · v0.2.0; badge/lógica pendientes)_
- [ ] T-49 Emails al dueño

### HU-20 — Confirmar al cliente
Como cliente, quiero recibir por email la confirmación con mi código de retiro.
- [ ] T-50 Email de confirmación al cliente

---

## E11 — Panel y dashboard del dueño

### HU-21 — Dashboard
Como dueño, quiero ver de un vistazo reservas activas con vencimiento, ventas del mes y señas pendientes de retiro.
- [ ] T-51 Endpoint `GET /panel/dashboard`
- [ ] T-52 UI de dashboard

### HU-22 — Gestión de reembolsos
Como dueño, quiero ver/ejecutar los reembolsos de seña (rechazos y cancelaciones).
- [ ] T-53 Vista de reembolsos + estados MP

---

## Notas
- **Milestone 0 (setup) completado en `v0.2.0`:** monorepo `backend/` (Spring Boot + Flyway) y `frontend/` (React + Vite), migración inicial `V1__init.sql` con **todas** las entidades del §4, perfiles dev/prod con secrets por variables de entorno, `docker-compose` para Postgres y health-check E2E. Las tasks T-10/T-22/T-43/T-48 quedan **sin marcar** porque solo se hizo su parte de entidades/migración (ver notas inline); sus endpoints/UI/lógica siguen pendientes.
- El orden sugerido de implementación (ver `registro-releases.md`): E1 → E3 (catálogo lectura) → E2 → E4/E5 → E6 → E8 → E9 → E10/E11 → E7.
- Las historias del cliente dependen de E1 (auth verificada, R-12).

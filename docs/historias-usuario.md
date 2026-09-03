# Backlog — Historias de usuario y tasks (Módulo de Vinilos)

> **Notas de desarrollo.** Backlog derivado de `spec-modulo-vinilos.md`. Se marca el avance con checkboxes: `[ ]` pendiente, `[x]` hecho. Cada historia lista sus criterios de aceptación (CA) y sus tasks técnicas. Al completar, cruzar con `registro-releases.md`.
> Convención de IDs: `HU-##` historias, `T-##` tasks. Referencias a reglas (`R-#`) y flujos (`Flujo X`) apuntan a la spec de dominio.

> **Cambio de alcance (spec v0.2):** se elimina la **seña** (épica E4). El único modo online es la **compra directa** (E5). El "retiro/resolución" (E6) pasa a ser **entrega** (retiro con código / envío por correo). Se agrega **E12 — Pedidos de búsqueda**.

## Estado global (resumen)
- [x] E1 — Cuentas y autenticación _(email/contraseña + verificación + JWT + login con Google + cuenta)_
- [x] E2 — Publicación y gestión de vinilos (dueño)
- [x] E3 — Catálogo y descubrimiento (cliente)
- [~] ~~E4 — Seña~~ **ELIMINADA (v0.2)** — no hay seña ni reserva.
- [x] E5 — Compra directa (100%) + elección de entrega _(flujo completo con gateway de dev; falta cablear MP real — T-34)_
- [x] E6 — Entrega (retiro con código / envío por correo)
- [x] E7 — Venta walk-in
- [ ] E8 — Cupones
- [ ] E9 — Jobs automáticos
- [ ] E10 — Notificaciones
- [ ] E11 — Panel y dashboard del dueño
- [ ] E12 — Pedidos de búsqueda

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
- [x] T-11 Endpoint `POST /vinilos` + subida de fotos a storage _(fotos en MinIO / object storage S3-compatible; primera = portada)_
- [x] T-12 UI de alta en el panel _(crear → redirige a edición para cargar fotos)_

### HU-05 — Editar / pausar vinilo
Como dueño, quiero editar datos o pausar/reactivar una publicación.
- CA: `pausado` sale del catálogo público y puede volver a `disponible`.
- [x] T-13 Endpoints `PUT /vinilos/{id}`, `PATCH /vinilos/{id}/pausar` _(pausar togglea disponible↔pausado; bloquea si está reservado/vendido)_
- [x] T-14 UI de edición y pausa _(editar datos + gestión de fotos; pausar/reactivar desde la lista)_

### HU-06 — Gestionar géneros
Como dueño, quiero una lista base de géneros y poder agregar nuevos.
- [x] T-15 CRUD mínimo de `Genero` _(alta + listado en el panel; edición/borrado no incluidos por ahora)_

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

> **E4 (Seña) — ELIMINADA en la spec v0.2.** Ya no existe la seña ni la reserva. HU-10 (señar) y HU-11 (cancelar reserva) quedan sin efecto. Las tareas T-22…T-31 no se implementan. La lógica de órdenes/pago/webhook/código/concurrencia se cubre en **E5 (compra directa)**.

---

## E5 — Compra directa (100%) + elección de entrega

### HU-12 — Comprar directo
Como cliente verificado, quiero comprar uno o varios vinilos pagando el 100%.
- CA: venta **final** (**R-6**); pago 100% online por MP.
- CA: al aprobarse el pago (**webhook** idempotente), vinilos → `vendido`; si `retiro`, se genera **código de retiro** (R-13); si `envio`, se guardan los datos de envío.
- CA: **bloqueo de concurrencia** — dos usuarios no compran la misma pieza (**R-11**).
- CA: elige **modo de entrega** (retiro / envío) en el checkout (**R-14**).
- [x] T-32 Entidades/enum de órdenes v0.2 (sin seña) + migración `V2` (quita `senable`/`RESERVADO`/`tipo`/resolución; agrega `modo_entrega`, datos de envío, `bloqueo_hasta`)
- [x] T-33 Endpoint `POST /ordenes` (compra + modo de entrega)
- [~] T-34 Integración MP: pago 100% _(hecho con **gateway de dev** simulado; **Mercado Pago real** — Checkout Pro sandbox — pendiente de credenciales, se activa sin tocar la lógica)_
- [x] T-35 Confirmación idempotente + generación de código (retiro) _(vía `PaymentGateway`; endpoint dev `POST /dev/pagos/simular`; el webhook MP real queda como stub para cablear)_
- [x] T-36 Bloqueo de concurrencia (R-11) _(lock pesimista + hold temporal del vinilo)_
- [x] T-37 UI de checkout (selección de entrega + pago simulado)

---

## E6 — Entrega (retiro / envío)

### HU-13 — Confirmar entrega (retiro en el local)
Como dueño, quiero ingresar el código de retiro y confirmar la entrega.
- CA: ingreso el código → veo la orden y sus vinilos → **Confirmar entrega** → orden `entregada` (Flujo B).
- [x] T-38 Endpoint `GET /retiros/{codigo}` (buscar) + `POST /retiros/{codigo}/entregar` + `GET /admin/ordenes`
- [x] T-39 UI de retiro y entrega en el panel

### HU-14 — Despachar envío por correo
Como dueño, quiero ver los datos de envío de una orden y marcarla como despachada.
- CA: la orden `envio` muestra la dirección; al despacharla → orden `enviada` (Flujo C).
- CA: el despacho es **manual** (el dueño va al correo); integración con Correo Argentino/OCA = futuro (D-7).
- [x] T-40 Endpoint `POST /admin/ordenes/{id}/despachar`
- [x] T-41 UI de envíos en el panel (ver dirección + marcar despachado)

---

## E7 — Venta walk-in

### HU-15 — Vender en efectivo en el local
Como dueño, quiero marcar un vinilo disponible como vendido en efectivo (**Flujo E**).
- CA: solo sobre `disponible` (**R-7**); sale del catálogo de inmediato.
- [x] T-59 Endpoint `POST /vinilos/{id}/venta-efectivo` _(también respeta el hold de checkout online)_
- [x] T-60 UI de venta walk-in _(botón "Venta efectivo" en la lista del panel)_

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

> **HU-17 (Vencer reservas) — ELIMINADA (v0.2):** no hay reservas ni vencimiento.

### HU-18 — Ocultar vendidos
Como sistema, quiero ocultar del catálogo los vendidos con más de 30 días (**Flujo E**).
- CA: no se borra el registro; se filtra en el catálogo (R-9).
- [ ] T-47 Job programado de ocultamiento + filtro en queries _(el filtro de visibilidad del catálogo ya se aplica en las queries desde M1)_

---

## E10 — Notificaciones

### HU-19 — Avisar al dueño
Como dueño, quiero enterarme de nuevas **compras** y **pedidos de búsqueda** por el panel y por email.
- CA: registro en `NotificacionDueno` (cartelito) + email. WhatsApp = fase 2.
- [ ] T-48 Entidad `NotificacionDueno` + badge en panel _(migración/entidad hecha en M0 · v0.2.0; badge/lógica pendientes)_
- [ ] T-49 Emails al dueño

### HU-20 — Emails al cliente
Como cliente, quiero recibir por email la confirmación de compra (con el código si es retiro) y el aviso cuando encuentran un vinilo que pedí.
- [ ] T-50 Email de confirmación de compra + email de "vinilo encontrado"

---

## E11 — Panel y dashboard del dueño

### HU-21 — Dashboard
Como dueño, quiero ver de un vistazo las ventas del mes, las órdenes pendientes de entrega/despacho y los pedidos de búsqueda abiertos.
- [ ] T-51 Endpoint `GET /panel/dashboard`
- [ ] T-52 UI de dashboard

### HU-22 — Gestión de reembolsos
Como dueño, quiero ver/ejecutar reembolsos excepcionales (devoluciones acordadas).
- [ ] T-53 Vista de reembolsos + estados MP

---

## E12 — Pedidos de búsqueda

### HU-23 — Pedir un vinilo que no está
Como cliente verificado, quiero pedir un disco que no encuentro para que el dueño intente conseguirlo (**R-15**).
- CA: solo clientes con cuenta verificada; cargo título, artista y notas (opcional).
- CA: al crearlo, el dueño recibe notificación (cartelito + email); yo lo veo en estado `buscando` en "Mi cuenta".
- [ ] T-54 Entidad `PedidoBusqueda` + migración
- [ ] T-55 Endpoints `POST /pedidos-busqueda`, `GET /pedidos-busqueda/mios`
- [ ] T-56 UI "¿No lo encontrás? Pedilo" en el catálogo + "Mis pedidos" en la cuenta

### HU-24 — Resolver un pedido de búsqueda
Como dueño, quiero ver los pedidos abiertos y marcarlos encontrado/no encontrado.
- CA: al marcar **encontrado**, el sistema le manda un email al cliente y me muestra su contacto para escribirle (WhatsApp/mail).
- CA: al marcar **no encontrado**, se avisa al cliente.
- [ ] T-57 Endpoints `GET /admin/pedidos-busqueda`, `POST /admin/pedidos-busqueda/{id}/resolver`
- [ ] T-58 UI de gestión de pedidos en el panel (+ email automático al cliente)

---

## Notas
- **Cambio de alcance (spec v0.2):** sin seña. El modelo de órdenes de M0 (`V1__init.sql`) tenía las entidades pensadas para seña; al implementar **E5** hay que hacer una **migración nueva** que lo adapte a compra directa (quitar `senable`, estado `reservado`, `Orden.tipo`, `ItemOrden.estadoItem`, `Pago.tipo`; agregar `Orden.modoEntrega` + datos de envío; agregar `PedidoBusqueda`). Ver T-32 y T-54.
- Milestones 0–2 completados (`v0.2.0`–`v0.4.0`): setup, cuentas+catálogo, gestión de vinilos con fotos en MinIO.
- Orden sugerido restante: **E5** (compra) → **E6** (entrega) → **E12** (pedidos de búsqueda) → **E8/E9/E10** (cupones/jobs/notif) → **E11** (panel/hardening). E7 (walk-in) puede ir junto con E6.
- Las historias del cliente dependen de E1 (auth verificada, R-12).

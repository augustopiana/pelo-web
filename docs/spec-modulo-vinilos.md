# Especificación — Módulo de Venta de Vinilos

> Borrador v0.2 — insumo para Spec Driven Development (SDD)
> Alcance de este documento: **únicamente el módulo de vinilos**. El módulo de turnos/peluquería se especifica por separado, pero se dejan preparados los puntos de contacto (cupones de descuento en corte).
>
> **Cambio de alcance (v0.2):** se elimina la **seña/reserva**. El único modo de venta online es la **compra directa** (100%). Se agrega la **entrega por retiro en el local o envío por correo**, y los **pedidos de búsqueda** de vinilos que no están en el catálogo.

---

## 1. Objetivo y contexto

Hoy el dueño publica y vende vinilos por Instagram y cierra la venta por DM. El objetivo del sistema es reemplazar ese flujo con una web que ofrezca:

- Un **catálogo público** ordenado, con buscador y filtros.
- **Stock real**: cada disco es una pieza única que se marca vendida automáticamente.
- **Cobro online** de las compras vía Mercado Pago.
- **Entrega** por retiro en el local o envío por correo.
- **Pedidos de búsqueda**: si un cliente no encuentra un disco, lo pide y el dueño intenta conseguirlo.
- Un **panel** para que el dueño cargue discos y gestione ventas, entregas y pedidos.

El diferencial frente a Instagram es: catálogo navegable, control de stock, cobro automático y trazabilidad de cada operación.

---

## 2. Alcance

### Dentro del MVP
- Catálogo público con búsqueda, filtros y orden.
- Cuentas de cliente (email+contraseña y Google), con verificación de email.
- Publicación y gestión de vinilos (pieza única).
- Flujo de **compra directa** (100%, venta final).
- Flujo de **venta en efectivo en el local** (walk-in).
- **Entrega**: retiro en el local con **código**, o **envío por correo** (despacho manual del dueño).
- **Pedidos de búsqueda** de vinilos no encontrados (solo clientes con cuenta).
- **Cupones** de descuento en corte de pelo (se generan acá; se redimen en el futuro módulo de turnos).
- Panel del dueño con dashboard, gestión de discos, órdenes, entregas y pedidos.
- Notificaciones: aviso en panel + email (al dueño) y emails al cliente (confirmación, vinilo encontrado).
- Comprobante interno de la operación.

### Fuera del MVP (fases futuras)
- Módulo de turnos/peluquería y **redención** de los cupones.
- **Integración con la API de Correo Argentino / OCA** (cotización de envío, etiquetas, tracking). En el MVP el envío es **manual**: la app guarda la dirección y el dueño despacha; el pago online cubre solo el precio del disco.
- **Carga por IA**: foto de tapa/contratapa → autocompletado de campos; el dueño solo pone precio y corrige.
- **WhatsApp automático** al dueño (fase 2, si el dueño lo aprueba; requiere API de WhatsApp Business con costo mensual).
- **CDs y cassettes** (el modelo deja el campo `formato` preparado).
- **Facturación electrónica** (AFIP/ARCA).

---

## 3. Actores y roles

| Rol | Descripción | Capacidades |
|-----|-------------|-------------|
| **Visitante** | No autenticado | Ver catálogo, buscar, filtrar, ver ficha. No puede comprar ni pedir búsquedas. |
| **Cliente** | Cuenta verificada | Comprar, elegir entrega (retiro/envío), ver sus órdenes y cupones, crear **pedidos de búsqueda** y ver su estado. |
| **Dueño / Admin** | Único administrador en el MVP | CRUD de vinilos, gestión de órdenes, entregas y envíos, ventas walk-in, resolución de pedidos de búsqueda, reembolsos, dashboard. |

> En el MVP el dueño es el único admin. El modelo de datos deja `rol` como campo extensible para sumar más operadores en el futuro.

---

## 4. Modelo de datos (entidades)

### 4.1 Usuario
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| nombre | texto | |
| email | texto | único |
| email_verificado | bool | requerido en `true` para operar |
| password_hash | texto | nulo si es solo Google |
| google_id | texto | nulo si es solo email/contraseña |
| telefono | texto | opcional; usado como contacto para pedidos de búsqueda y envíos |
| rol | enum | `cliente` / `admin` |
| created_at | timestamp | |

### 4.2 Vinilo
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| titulo | texto | álbum |
| artista | texto | |
| genero_id | FK Genero | lista controlada |
| anio | entero | |
| sello | texto | opcional |
| edicion_pais | texto | ej. "Edición original 🇦🇷 1986" |
| formato | enum | `vinilo` (fijo en MVP; preparado para `cd`, `cassette`) |
| estado_disco | enum | estado general del disco; **escala Goldmine**: `Mint (M)`, `Near Mint (NM)`, `VG++`, `VG+`, `VG`, `Good (G)`, `Poor (P)` |
| descripcion | texto largo | libre |
| precio | decimal (ARS) | público |
| descuento_corte_pct | entero | % de descuento en corte que otorga este vinilo (ej. 15) |
| estado | enum | `disponible` / `vendido` / `pausado` |
| fecha_publicacion | timestamp | usado para orden "más nuevos" |
| fecha_venta | timestamp | se setea al marcar vendido; controla el ocultamiento a 30 días |
| created_at / updated_at | timestamp | |

> **Cambio v0.2:** se elimina el campo `senable` (ya no hay seña; todo es compra directa) y el estado `reservado` (no hay reservas).

### 4.3 FotoVinilo
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| vinilo_id | FK Vinilo | |
| url | texto | |
| orden | entero | posición en la galería |
| es_portada | bool | la **primera** es la tapa; se muestra en el grid del catálogo |

### 4.4 Genero
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| nombre | texto | lista base editable por el dueño (Rock, Jazz, Folklore, etc.) |

### 4.5 Orden
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| usuario_id | FK Usuario | |
| estado | enum | ver máquina de estados §5.2 |
| total | decimal | suma de precios de los ítems (100% online) |
| monto_pagado | decimal | igual al total (compra directa) |
| modo_entrega | enum | `retiro` / `envio` |
| codigo_retiro | texto | solo `retiro`: **generado aleatoriamente, único, corto y legible** (ver R-13) |
| fecha_pago | timestamp | cuando se aprueba el pago (webhook) |
| fecha_entrega | timestamp | retiro: cuando el dueño confirma la entrega |
| fecha_despacho | timestamp | envío: cuando el dueño marca que lo despachó por correo |
| created_at | timestamp | |

> **Cambio v0.2:** se elimina el campo `tipo` (ya no hay `sena` vs `compra_directa`; todo es compra directa) y `fecha_vencimiento` (no hay reservas). Se agregan `modo_entrega`, `fecha_despacho` y los datos de envío (§4.11).

### 4.6 ItemOrden
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| orden_id | FK Orden | |
| vinilo_id | FK Vinilo | |
| precio | decimal | copia del precio al momento de la orden |
| descuento_corte_pct | entero | copia del % del vinilo al momento de la orden |

> **Cambio v0.2:** se elimina la resolución ítem por ítem (`estado_item`, `resto_pagado`, `metodo_resto`), que era propia de la seña. En compra directa, todos los ítems se venden al aprobarse el pago.

### 4.7 Pago
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| orden_id | FK Orden | |
| monto | decimal | total de la orden |
| medio | enum | `mercadopago` / `efectivo` |
| mp_payment_id | texto | id del pago en MP (si aplica) |
| estado | enum | `pendiente` / `aprobado` / `rechazado` |
| created_at | timestamp | |

> **Cambio v0.2:** se elimina `tipo` (`sena`/`resto`/`total`): siempre es el pago total.

### 4.8 Reembolso
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| pago_id | FK Pago | pago a devolver |
| orden_id | FK Orden | orden asociada |
| monto | decimal | monto devuelto |
| mp_refund_id | texto | id de la devolución en MP |
| motivo | texto | motivo libre (devolución excepcional decidida por el dueño) |
| created_at | timestamp | |

> **Cambio v0.2:** la compra directa es venta final; los reembolsos quedan como caso **excepcional** que ejecuta el dueño (p. ej. una devolución acordada). Se eliminan los motivos `rechazo_prueba` (no hay prueba) y `cancelacion_voluntaria` de reserva (no hay reserva).

### 4.9 Cupon
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| usuario_id | FK Usuario | |
| orden_id | FK Orden | orden que lo generó |
| porcentaje | entero | el **mayor** % entre los ítems de la orden |
| fecha_generacion | timestamp | |
| fecha_vencimiento | timestamp | generación + 2 meses |
| estado | enum | `activo` / `usado` / `vencido` |
| fecha_uso | timestamp | se completa al redimir (módulo turnos, futuro) |

### 4.10 NotificacionDueño
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| tipo | enum | `nueva_compra` / `nuevo_pedido_busqueda` |
| orden_id | FK Orden | nulo si es de un pedido de búsqueda |
| pedido_busqueda_id | FK PedidoBusqueda | nulo si es de una compra |
| leida | bool | alimenta el "cartelito" del panel |
| created_at | timestamp | |

### 4.11 Orden — datos de envío (solo `modo_entrega = envio`)
| Campo | Tipo | Notas |
|-------|------|-------|
| envio_nombre | texto | nombre de quien recibe |
| envio_telefono | texto | contacto |
| envio_direccion | texto | calle y número |
| envio_localidad | texto | |
| envio_provincia | texto | |
| envio_cp | texto | código postal |

> Se guardan como parte de la `Orden`. El despacho es **manual**: el dueño lleva el paquete al correo. La integración con Correo Argentino/OCA queda fuera del MVP.

### 4.12 PedidoBusqueda (nuevo)
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| usuario_id | FK Usuario | solo clientes con cuenta |
| titulo | texto | disco buscado |
| artista | texto | |
| notas | texto | opcional (edición, año, detalles) |
| estado | enum | `buscando` / `encontrado` / `no_encontrado` |
| created_at | timestamp | |
| fecha_resolucion | timestamp | cuando el dueño lo marca encontrado/no encontrado |

---

## 5. Máquinas de estado

### 5.1 Vinilo

```
                 ┌──────────────┐
        (dueño)  │   pausado    │  (dueño)
        ┌───────►│              │────────┐
        │        └──────────────┘        │
   ┌────┴──────┐                         ▼
   │ disponible│   compra directa (100%)  ┌──────────────┐
   │           │──────────────────────────►│   vendido    │
   └───────────┘   o venta efectivo walk-in│              │
                                           └──────┬───────┘
                                                  │ 30 días después
                                                  ▼
                                   (oculto del catálogo público,
                                    se conserva en DB)
```

| Transición | Disparador |
|-----------|-----------|
| disponible → vendido | Compra directa pagada (100%) **o** venta efectivo walk-in |
| disponible → pausado | El dueño lo pausa |
| pausado → disponible | El dueño lo reactiva |
| vendido → (oculto) | 30 días después de `fecha_venta`; permanece en la base para historial |

> **Cambio v0.2:** se elimina el estado `reservado` y todas sus transiciones (no hay seña ni reservas).

### 5.2 Orden (compra directa)

```
pendiente_pago ──(pago 100% aprobado)──► pagada ──┬─(retiro: entrega en el local)──► entregada
      │                                            └─(envío: despachado por correo)──► enviada
      └──(pago rechazado / no aprobado)──► cancelada
```

- **pendiente_pago:** creada, esperando el webhook de Mercado Pago.
- **pagada:** pago aprobado (por webhook). Vinilos → `vendido`. Si `retiro`, se genera el **código de retiro**.
- **entregada:** (retiro) el dueño confirmó la entrega en el local.
- **enviada:** (envío) el dueño marcó que despachó el paquete por correo.
- **cancelada:** el pago no se aprobó.

### 5.3 PedidoBusqueda

```
buscando ──(el dueño lo consigue)────► encontrado
    └──────(el dueño no lo consigue)──► no_encontrado
```

- **buscando:** creado por el cliente; el dueño fue notificado. El cliente ve "tu vinilo está siendo buscado".
- **encontrado:** el dueño lo consiguió → el sistema **avisa al cliente por email** y el dueño ve su contacto para escribirle (WhatsApp/mail).
- **no_encontrado:** el dueño no lo pudo conseguir → se avisa al cliente.

---

## 6. Reglas de negocio

> Numeración estable: las reglas **R-1…R-5** (seña, reserva, vencimiento, rechazo al probar, no presentarse) **quedan eliminadas** en v0.2. Se conservan los IDs para no romper referencias históricas. R-10 (no mezclar señables/no señables) también queda sin efecto.

- ~~**R-1…R-5**~~ **Eliminadas (v0.2):** eran de la seña/reserva.
- **R-6** La **compra directa** es el único modo de venta online: pago 100% online por Mercado Pago, venta **final**.
- **R-7** La **venta en efectivo walk-in** solo puede hacerse sobre vinilos en estado `disponible` (nunca sobre uno pausado/vendido).
- **R-8 (cupones):**
  - Se genera **1 cupón por compra** con venta confirmada.
  - Tope: **máximo 1 cupón cada 30 días corridos** desde la `fecha_generacion` del último cupón del cliente. Si ya obtuvo uno en los últimos 30 días, la compra se concreta igual pero **no genera cupón**.
  - El **porcentaje** del cupón es el **mayor %** entre los ítems de esa orden.
  - **Validez: 2 meses** desde la generación.
  - **Uso: 1 cupón por corte** (regla a aplicar en el módulo de turnos).
- **R-9** Un vinilo `vendido` se **oculta del catálogo público 30 días** después de la venta; el registro se conserva en la base para historial.
- ~~**R-10**~~ **Sin efecto (v0.2):** ya no hay vinilos señables vs no señables.
- **R-11 (concurrencia):** al iniciar el pago de una compra, el vinilo se **bloquea** para evitar que dos personas compren la misma pieza única. Si el pago no se aprueba o expira, se libera. Se verifica `disponible` y se cambia el estado atómicamente.
- **R-12** El cliente debe tener el **email verificado** para comprar y para crear pedidos de búsqueda.
- **R-13 (código de retiro):** en las órdenes con `modo_entrega = retiro`, al confirmarse el pago se genera un `codigo_retiro` con estas propiedades:
  - **Aleatorio**, generado con un algoritmo (no correlativo ni predecible).
  - **Único e irrepetible**: se verifica que no exista otro código vigente igual antes de asignarlo (reintentar si colisiona).
  - **Legible y entendible**: corto (6–8 caracteres), mayúsculas, alfabeto sin ambiguos (se excluyen `0/O`, `1/I/L`), agrupado (ej. `H7K-2P9`).
- **R-14 (entrega):** el cliente elige al comprar entre **retiro en el local** o **envío por correo**.
  - **Retiro:** se genera código de retiro; el dueño lo ingresa y confirma la entrega.
  - **Envío:** se capturan los **datos de envío** (§4.11); el dueño despacha **manualmente** por correo y marca la orden como `enviada`. El pago online cubre **solo el precio del disco**; el servicio de correo se gestiona con el correo por fuera.
- **R-15 (pedido de búsqueda):** solo un **cliente con cuenta verificada** puede crear un pedido de búsqueda.
  - Al crearse, **notifica al dueño** (cartelito + email).
  - Estados: `buscando` → `encontrado` / `no_encontrado`.
  - Al pasar a `encontrado`, el sistema **envía un email automático** al cliente y el dueño ve el **contacto** del cliente para escribirle por WhatsApp/mail.

---

## 7. Flujos principales

### Flujo A — Comprar directo (uno o varios vinilos)
1. El cliente (verificado) agrega uno o más vinilos disponibles y va a pagar.
2. Elige **modo de entrega**: retiro en el local o envío por correo (si es envío, carga los datos de envío).
3. El sistema bloquea esos vinilos (R-11) y crea una `Orden` en `pendiente_pago`.
4. Cobra el **100% del total** por Mercado Pago.
5. Con el pago aprobado (**webhook**): la orden pasa a `pagada`, cada vinilo pasa a `vendido` (con `fecha_venta`), se genera el **código de retiro** (si es retiro) y, si corresponde por R-8, el **cupón**.
6. El cliente recibe **email de confirmación** (con el código si es retiro). El dueño ve el **aviso en su panel + email**.

### Flujo B — Retiro en el local
1. El cliente se presenta con su **código**. El dueño lo ingresa en el panel.
2. El panel muestra la orden y sus vinilos; el dueño toca **Confirmar entrega** → orden `entregada`.

### Flujo C — Envío por correo
1. La orden con `modo_entrega = envio` queda `pagada` con los datos de envío.
2. El dueño prepara el paquete y lo **despacha manualmente** por Correo Argentino/OCA.
3. El dueño marca la orden como **despachada** → orden `enviada`.
4. (Futuro) Integración con la API del correo para cotización/etiqueta/tracking.

### Flujo D — Venta en efectivo walk-in
1. Una persona entra al local y compra un vinilo físico en el momento.
2. El dueño, desde el panel, marca ese vinilo (que debe estar `disponible`, R-7) como **vendido en efectivo** → vinilo `vendido` (`fecha_venta`), sin orden online.
3. El sistema lo saca inmediatamente del catálogo público.

### Flujo E — Ocultamiento de vendidos (proceso automático)
1. Un job oculta del catálogo público los vinilos `vendido` cuya `fecha_venta` tenga más de 30 días. El registro permanece en la base.

### Flujo F — Pedido de búsqueda
1. Un cliente verificado que no encuentra un disco crea un **pedido de búsqueda** (título, artista, notas).
2. El pedido queda en `buscando`; el dueño recibe **notificación** (cartelito + email). El cliente ve el estado en "Mi cuenta".
3. El dueño intenta conseguirlo. Cuando resuelve:
   - **Encontrado** → el sistema **avisa al cliente por email**; el dueño ve el contacto del cliente y lo contacta (WhatsApp/mail) para coordinar la venta.
   - **No encontrado** → se avisa al cliente que no se pudo conseguir.

---

## 8. Catálogo público (cliente/visitante)

- **Grilla** con foto de **tapa** (portada), título, artista, precio y estado del disco; indicador visual de `vendido` (visible hasta el ocultamiento a 30 días).
- **Ficha del vinilo:** galería de fotos, todos los datos, precio, **descuento en corte** que otorga, y botón **Comprar**.
- **Buscador** por texto (título/artista).
- **Filtros:** artista, género, rango de precio, estado del disco.
- **Orden:** "más nuevos" por `fecha_publicacion` (por defecto).
- **Pedido de búsqueda:** si el cliente no encuentra lo que busca, un espacio del tipo **"¿No lo encontrás? Pedilo"** le permite crear un pedido de búsqueda (requiere cuenta verificada).
- Precios **públicos**.
- **Explicación de la escala Goldmine:** tooltip/ícono de ayuda junto al estado en la ficha y/o una **página de ayuda o glosario** que describa qué significa cada sigla.

---

## 9. Panel del dueño

- **Dashboard:** ventas del mes, órdenes por estado (pendientes de entrega/despacho), pedidos de búsqueda abiertos, y el "cartelito" de notificaciones sin leer.
- **Gestión de vinilos:** cargar, editar, **pausar**/reactivar; definir `descuento_corte_pct`; subir fotos (la primera = tapa).
- **Órdenes:** ver órdenes por estado; **retiro** (ingresar código y confirmar entrega) y **envío** (ver datos de envío y marcar despachado).
- **Pedidos de búsqueda:** ver los abiertos, marcar **encontrado**/**no encontrado**, ver el contacto del cliente.
- **Venta walk-in:** marcar un vinilo disponible como vendido en efectivo (Flujo D).
- **Reembolsos:** ejecutar una devolución excepcional (integración con MP) si hace falta.

---

## 10. Notificaciones

| Evento | Al dueño | Al cliente |
|--------|----------|-----------|
| Nueva compra | Cartelito en panel + email | Email de confirmación (con código si es retiro) |
| Nuevo pedido de búsqueda | Cartelito en panel + email | — |
| Vinilo encontrado | (lo marca el dueño) | **Email automático** de que se encontró; el dueño además lo contacta por WhatsApp/mail |

> WhatsApp automático al dueño queda para **fase 2** (requiere API de WhatsApp Business, con costo mensual y aprobación de plantillas).

---

## 11. Decisiones (a confirmar antes de implementar / ya tomadas)

### Decisiones ya tomadas
- **D-1 (resuelta) — Escala de estado del disco.** Se adopta la **escala Goldmine** (Mint, Near Mint, VG++, VG+, VG, Good, Poor) como lista cerrada, con explicación de siglas en la web (§8).
- **D-2 (resuelta) — Stack técnico.** Backend Java + Spring Boot, frontend React. Detalle en `documentacion-tecnica.md`.
- **D-4 (resuelta, v0.2) — Sin seña.** Se elimina la seña/reserva; el único modo online es la **compra directa** (100%).
- **D-5 (resuelta, v0.2) — Entrega.** El cliente elige **retiro en el local** (con código) o **envío por correo** (despacho manual del dueño; el pago online cubre solo el disco).
- **D-6 (resuelta, v0.2) — Pedidos de búsqueda.** Solo clientes con cuenta; al encontrarlo, email automático al cliente + contacto manual del dueño.

### Decisiones abiertas / a explorar
- **D-7 — Integración con el correo (Correo Argentino/OCA).** Cotización, etiquetas y tracking automáticos. Requiere cuenta/contrato con el correo y sus APIs. Queda como **exploración futura**; en el MVP el envío es manual.

---

## 12. Puntos de contacto con el módulo de turnos (futuro)

- El **Cupon** generado acá se redime en el módulo de turnos: **1 cupón por corte**, no acumulable con otros cupones en el mismo turno.
- Validez del cupón: **2 meses** desde su generación.
- El % del cupón se aplica sobre el precio del corte (a definir en la spec de turnos).

# Especificación — Módulo de Venta de Vinilos

> Borrador v0.1 — insumo para Spec Driven Development (SDD)
> Alcance de este documento: **únicamente el módulo de vinilos**. El módulo de turnos/peluquería se especifica por separado, pero se dejan preparados los puntos de contacto (cupones de descuento en corte).

---

## 1. Objetivo y contexto

Hoy el dueño publica y vende vinilos por Instagram y cierra la venta por DM. El objetivo del sistema es reemplazar ese flujo con una web que ofrezca:

- Un **catálogo público** ordenado, con buscador y filtros.
- **Stock real**: cada disco es una pieza única que se marca reservada/vendida automáticamente.
- **Cobro online** de señas y compras vía Mercado Pago.
- Un **panel** para que el dueño cargue discos y gestione reservas, ventas y retiros.

El diferencial frente a Instagram es: catálogo navegable, control de stock, cobro automático de seña y trazabilidad de cada operación.

---

## 2. Alcance

### Dentro del MVP
- Catálogo público con búsqueda, filtros y orden.
- Cuentas de cliente (email+contraseña y Google), con verificación de email.
- Publicación y gestión de vinilos (pieza única).
- Flujo de **seña** (50%, con prueba en el local).
- Flujo de **compra directa** (100%, sin prueba).
- Flujo de **venta en efectivo en el local** (walk-in).
- Retiro con **código** y resolución por vinilo.
- **Cupones** de descuento en corte de pelo (se generan acá; se redimen en el futuro módulo de turnos).
- Panel del dueño con dashboard, gestión de discos y de órdenes.
- Notificaciones: aviso en panel + email (al dueño) y email de confirmación (al cliente).
- Solo **retiro en el local**.
- Comprobante interno de la operación.

### Fuera del MVP (fases futuras)
- Módulo de turnos/peluquería y **redención** de los cupones.
- **Envío por correo** (a todo el país o por zona).
- **Carga por IA**: foto de tapa/contratapa → autocompletado de campos; el dueño solo pone precio y corrige.
- **WhatsApp automático** al dueño (fase 2, si el dueño lo aprueba; requiere API de WhatsApp Business con costo mensual).
- **CDs y cassettes** (el modelo deja el campo `formato` preparado).
- **Facturación electrónica** (AFIP/ARCA).

---

## 3. Actores y roles

| Rol | Descripción | Capacidades |
|-----|-------------|-------------|
| **Visitante** | No autenticado | Ver catálogo, buscar, filtrar, ver ficha. No puede señar/comprar. |
| **Cliente** | Cuenta verificada | Señar, comprar, ver sus órdenes/estados/cupones, cancelar reservas antes de probar. |
| **Dueño / Admin** | Único administrador en el MVP | CRUD de vinilos, gestión de órdenes y retiros, ventas walk-in, reembolsos, dashboard. |

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
| telefono | texto | opcional |
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
| descripcion | texto largo | libre (acá pueden ir detalles de tapa/sobre si el dueño quiere) |
| precio | decimal (ARS) | público |
| descuento_corte_pct | entero | % de descuento en corte que otorga este vinilo (ej. 15) |
| senable | bool | `true`: solo se puede señar. `false`: solo compra directa. |
| estado | enum | `disponible` / `reservado` / `vendido` / `pausado` |
| fecha_publicacion | timestamp | usado para orden "más nuevos" |
| fecha_venta | timestamp | se setea al marcar vendido; controla el ocultamiento a 30 días |
| created_at / updated_at | timestamp | |

> **Regla clave:** `senable` bifurca el modo de compra. Un vinilo señable **solo** admite seña; uno no señable **solo** admite compra directa online. Ambos, además, pueden venderse en efectivo walk-in mientras estén `disponible`.

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
| tipo | enum | `sena` / `compra_directa` |
| estado | enum | ver máquina de estados §5.2 |
| total | decimal | suma de precios de los ítems |
| monto_pagado | decimal | seña (50%) o total (100%) |
| codigo_retiro | texto | **generado aleatoriamente, único e irrepetible, corto y legible** (ver R-13); se muestra al cliente y lo ingresa el dueño |
| fecha_vencimiento | timestamp | solo `sena`: creación + 7 días |
| fecha_cierre | timestamp | cuando se resuelven todos los ítems |
| created_at | timestamp | |

> **Restricción:** una orden **no** mezcla vinilos señables y no señables (R-10). Si el cliente quiere señar unos y comprar otros directo, son órdenes separadas.

### 4.6 ItemOrden
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| orden_id | FK Orden | |
| vinilo_id | FK Vinilo | |
| precio | decimal | copia del precio al momento de la orden |
| descuento_corte_pct | entero | copia del % del vinilo al momento de la orden |
| estado_item | enum | `pendiente` / `vendido` / `rechazado` / `vencido` |
| resto_pagado | bool | solo seña |
| metodo_resto | enum | `efectivo` / `online` (nulo hasta que se pague) |

### 4.7 Pago
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| orden_id | FK Orden | |
| tipo | enum | `sena` / `resto` / `total` |
| monto | decimal | |
| medio | enum | `mercadopago` / `efectivo` |
| mp_payment_id | texto | id del pago en MP (si aplica) |
| estado | enum | `pendiente` / `aprobado` / `rechazado` |
| created_at | timestamp | |

### 4.8 Reembolso
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| pago_id | FK Pago | seña a devolver |
| item_id | FK ItemOrden | vinilo asociado (en órdenes multi-vinilo la devolución es por ítem) |
| monto | decimal | 100% de la seña de ese ítem |
| mp_refund_id | texto | id de la devolución en MP |
| motivo | enum | `rechazo_prueba` / `cancelacion_voluntaria` |
| created_at | timestamp | |

> **Nota financiera (verificada con la doc de Mercado Pago):** al hacer una devolución total o parcial, MP **no cobra la comisión** del pago recibido y admite devolver **hasta 90 días** después de la venta. Como la reserva dura 7 días, devolver la seña no tiene costo para el dueño y entra holgado en los plazos.

### 4.9 Cupon
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| usuario_id | FK Usuario | |
| orden_id | FK Orden | orden que lo generó |
| porcentaje | entero | el **mayor** % entre los ítems vendidos de la orden |
| fecha_generacion | timestamp | |
| fecha_vencimiento | timestamp | generación + 2 meses |
| estado | enum | `activo` / `usado` / `vencido` |
| fecha_uso | timestamp | se completa al redimir (módulo turnos, futuro) |

### 4.10 NotificacionDueño
| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | |
| tipo | enum | `nueva_sena` / `nueva_compra` / `reserva_por_vencer` … |
| orden_id | FK Orden | |
| leida | bool | alimenta el "cartelito" del panel |
| created_at | timestamp | |

---

## 5. Máquinas de estado

### 5.1 Vinilo

```
                 ┌──────────────┐
        (dueño)  │   pausado    │  (dueño)
        ┌───────►│              │────────┐
        │        └──────────────┘        │
        │                                ▼
   ┌────┴──────┐   seña pagada    ┌──────────────┐
   │ disponible│─────────────────►│  reservado   │
   │           │                  │              │
   └────┬──────┘◄─────────────────└──────┬───────┘
        │        rechazo al probar /      │
        │        cancelación /            │ confirma y paga resto
        │        vencimiento reserva      ▼
        │                          ┌──────────────┐
        │  compra directa (100%)   │              │
        └─────────────────────────►│   vendido    │
        │  o venta efectivo walk-in │              │
        │                          └──────┬───────┘
        │                                 │ 30 días después
        │                                 ▼
        │                          (oculto del catálogo público,
        │                           se conserva en DB)
```

| Transición | Disparador |
|-----------|-----------|
| disponible → reservado | Se cobra la seña (50%) de un vinilo señable |
| disponible → vendido | Compra directa pagada (100%) **o** venta efectivo walk-in |
| disponible → pausado | El dueño lo pausa |
| pausado → disponible | El dueño lo reactiva |
| reservado → vendido | El cliente prueba, le gusta y paga el resto |
| reservado → disponible | Rechazo al probar / cancelación voluntaria / vencimiento de la reserva (7 días) |
| vendido → (oculto) | 30 días después de `fecha_venta`; permanece en la base para historial |

### 5.2 Orden

**Orden de seña**
```
pendiente_pago ──(seña aprobada)──► activa ──(todos los ítems resueltos)──► cerrada
      │                                │
      │                                ├──(cliente cancela antes de probar)──► cancelada
      │                                └──(vence a los 7 días sin retiro)─────► vencida
      └──(seña no se aprueba)──► cancelada
```

**Orden de compra directa**
```
pendiente_pago ──(pago 100% aprobado)──► pagada ──(entrega en el local)──► entregada
      └──(pago rechazado)──► cancelada
```

Cada **ItemOrden** de una orden de seña se resuelve de forma independiente:
`pendiente → vendido` (le gustó y pagó) / `rechazado` (no le gustó → reembolso) / `vencido` (no se presentó).
La orden pasa a `cerrada` cuando no quedan ítems `pendiente`.

---

## 6. Reglas de negocio

- **R-1** La seña es el **50%** del total de la orden, cobrada 100% online por Mercado Pago.
- **R-2** La reserva **vence a los 7 días**. Al vencer: los vinilos vuelven a `disponible` y la seña **se pierde**.
- **R-3** Si al retirar el cliente prueba un vinilo y **no le gusta**, se le **devuelve la seña de ese ítem** (100%) y el vinilo vuelve a `disponible`. **Quién lo marca:** el **dueño**, desde su panel, en el momento del retiro (es él quien ingresó el código y opera el panel, con el cliente presente). El cliente **no** puede marcar el rechazo por su cuenta; así se evita que alguien lo declare sin haber probado el disco. La acción queda registrada con fecha y dispara la devolución automática por Mercado Pago.
- **R-4** Si el cliente **cancela la reserva antes de ir a probar** (se arrepiente), se le **devuelve la seña** y los vinilos vuelven a `disponible`. **Quién lo marca:** el **cliente**, desde su cuenta, mientras la reserva siga vigente (antes del vencimiento y antes de presentarse a probar).
- **R-5** Si el cliente **no se presenta** y vence la semana, **pierde la seña** (único caso de pérdida).
- **R-6** La **compra directa** existe solo para vinilos **no señables**: pago 100% online, venta **final sin prueba**.
- **R-7** La **venta en efectivo walk-in** solo puede hacerse sobre vinilos en estado `disponible` (nunca sobre uno reservado/pausado/vendido).
- **R-8 (cupones):**
  - Se genera **1 cupón por compra** con venta confirmada.
  - Tope: **máximo 1 cupón cada 30 días corridos** desde la `fecha_generacion` del último cupón del cliente. Si ya obtuvo uno en los últimos 30 días, la compra se concreta igual pero **no genera cupón**.
  - El **porcentaje** del cupón es el **mayor %** entre los ítems **vendidos/confirmados** de esa orden (los rechazados no cuentan).
  - **Validez: 2 meses** desde la generación.
  - **Uso: 1 cupón por corte** (regla a aplicar en el módulo de turnos).
- **R-9** Un vinilo `vendido` se **oculta del catálogo público 30 días** después de la venta; el registro se conserva en la base para historial.
- **R-10** Una orden **no mezcla** vinilos señables y no señables.
- **R-11 (concurrencia):** al iniciar el pago de una seña/compra, el vinilo se **bloquea** para evitar que dos personas reserven la misma pieza única. Si el pago no se aprueba o expira, se libera.
- **R-12** El cliente debe tener el **email verificado** para señar o comprar.
- **R-13 (código de retiro):** al confirmarse el pago de una orden se genera un `codigo_retiro` con estas propiedades:
  - **Aleatorio**, generado con un algoritmo (no correlativo ni predecible a partir del id de la orden).
  - **Único e irrepetible**: se verifica que no exista otro código vigente igual antes de asignarlo (reintentar si colisiona).
  - **Legible y entendible**: corto (sugerido 6–8 caracteres), en mayúsculas, con un alfabeto sin caracteres ambiguos (se excluyen `0/O`, `1/I/L`), y puede mostrarse agrupado (ej. `H7K-2P9`) para facilitar el dictado y la carga manual.

---

## 7. Flujos principales

### Flujo A — Señar (uno o varios vinilos señables)
1. El cliente (verificado) agrega uno o más vinilos **señables** y va a pagar.
2. El sistema bloquea esos vinilos (R-11) y crea una `Orden` tipo `sena` en `pendiente_pago`.
3. Cobra el **50% del total** por Mercado Pago.
4. Con el pago aprobado: la orden pasa a `activa`, cada vinilo pasa a `reservado`, se fija `fecha_vencimiento` (+7 días) y se genera el **código de retiro**.
5. El cliente recibe **email de confirmación** con el código. El dueño ve el **aviso en su panel + email**.

### Flujo B — Comprar directo (uno o varios vinilos no señables)
1. El cliente agrega uno o más vinilos **no señables** y va a pagar.
2. Se crea `Orden` tipo `compra_directa` en `pendiente_pago`; se bloquean los vinilos.
3. Cobra el **100%** por Mercado Pago.
4. Con el pago aprobado: orden `pagada`, vinilos `vendido` (con `fecha_venta`), se genera **código de retiro** y (si corresponde por R-8) el **cupón**.
5. Email de confirmación al cliente + aviso al dueño.

### Flujo C — Retiro y resolución de una seña (por vinilo)
1. El cliente se presenta con su **código**. El dueño lo ingresa en el panel.
2. El panel muestra **todos los vinilos de esa orden**. Por cada uno, el dueño registra:
   - **Confirmar venta** → cobra el resto (efectivo u online en el momento) → ítem `vendido`, vinilo `vendido` (`fecha_venta`).
   - **Rechazado** → dispara **devolución de la seña de ese ítem** por MP → ítem `rechazado`, vinilo `disponible`.
3. Cuando no quedan ítems `pendiente`, la orden pasa a `cerrada`.
4. Al cierre, si hay al menos un ítem `vendido`, se evalúa R-8 y (si corresponde) se genera **1 cupón** con el **mayor %** de los ítems vendidos.

### Flujo D — Retiro de compra directa
1. El cliente se presenta con su código. El dueño lo ingresa.
2. El panel muestra los vinilos; el dueño toca **Confirmar entrega** → orden `entregada`.

### Flujo E — Venta en efectivo walk-in
1. Una persona entra al local y compra un vinilo físico en el momento.
2. El dueño, desde el panel, marca ese vinilo (que debe estar `disponible`, R-7) como **vendido en efectivo** → vinilo `vendido` (`fecha_venta`), sin orden online.
3. El sistema lo saca inmediatamente del catálogo público.

### Flujo F — Vencimiento de reserva (proceso automático)
1. Un job detecta órdenes de seña `activa` con `fecha_vencimiento` pasada y sin retiro.
2. Marca los ítems `pendiente` como `vencido`, los vinilos vuelven a `disponible`, la orden pasa a `vencida`.
3. La seña **se pierde** (no hay devolución).

### Flujo G — Ocultamiento de vendidos (proceso automático)
1. Un job oculta del catálogo público los vinilos `vendido` cuya `fecha_venta` tenga más de 30 días. El registro permanece en la base.

---

## 8. Catálogo público (cliente/visitante)

- **Grilla** con foto de **tapa** (portada), título, artista, precio y estado del disco; indicador visual de `reservado` / `vendido` (visibles hasta el ocultamiento a 30 días) y de si es señable o compra directa.
- **Ficha del vinilo:** galería de fotos, todos los datos, precio, **descuento en corte** que otorga, y botón **Señar** o **Comprar** según `senable`.
- **Buscador** por texto (título/artista).
- **Filtros:** artista, género, rango de precio, estado del disco.
- **Orden:** "más nuevos" por `fecha_publicacion` (por defecto).
- Precios **públicos**.
- **Explicación de la escala Goldmine:** como el estado del disco usa siglas (M, NM, VG++, etc.), la web debe ofrecer al cliente una **explicación accesible de la escala** — por ejemplo, un tooltip/ícono de ayuda junto al estado en la ficha y/o una **página de ayuda o glosario** que describa qué significa cada sigla. El objetivo es que el cliente entienda el estado sin conocer la jerga de coleccionista.

---

## 9. Panel del dueño

- **Dashboard:** reservas activas con su vencimiento, ventas del mes, vinilos con seña **pendiente de retiro**, y el "cartelito" de notificaciones sin leer.
- **Gestión de vinilos:** cargar, editar, **pausar**/reactivar; definir `senable` y `descuento_corte_pct`; subir fotos (la primera = tapa).
- **Órdenes:** ver órdenes por estado; ingresar **código de retiro** y resolver ítem por ítem (Flujo C/D).
- **Venta walk-in:** marcar un vinilo disponible como vendido en efectivo (Flujo E).
- **Reembolsos:** ejecutar la devolución de seña (integración con MP) para rechazos y cancelaciones.

---

## 10. Notificaciones

| Evento | Al dueño | Al cliente |
|--------|----------|-----------|
| Nueva seña | Cartelito en panel + email | Email de confirmación con código |
| Nueva compra directa | Cartelito en panel + email | Email de confirmación con código |
| Reserva por vencer | Cartelito en panel | (opcional a definir en fase 2) |

> WhatsApp automático al dueño queda para **fase 2** (requiere API de WhatsApp Business, con costo mensual y aprobación de plantillas).

---

## 11. Decisiones abiertas (a confirmar antes de implementar)

- **D-3 — Recordatorio de reserva por vencer al cliente** (email el día previo al vencimiento): incluido o no en el MVP.

### Decisiones ya tomadas
- **D-1 (resuelta) — Escala de estado del disco.** Se adopta la **escala Goldmine** (Mint, Near Mint, VG++, VG+, VG, Good, Poor) como lista cerrada, con un único estado general por disco. La web debe **explicar las siglas al cliente** (tooltip en la ficha y/o página de ayuda/glosario), según §8.
- **D-2 (resuelta) — Stack técnico.** Definido: **backend Java + Spring Boot, frontend React**. Todo el detalle técnico (arquitectura, entidades, integraciones, jobs, despliegue) se documenta **por separado**, en las notas de desarrollo, para no mezclar dominio con implementación. Ver `documentacion-tecnica.md`.

> **Nota sobre los documentos del proyecto.** Esta spec describe **el dominio y la página web** (el "qué"). Las notas de desarrollo del "cómo" y el seguimiento viven en archivos aparte: `documentacion-tecnica.md` (diseño técnico), `registro-releases.md` (releases por iteración) e `historias-usuario.md` (backlog con estado de tareas).

---

## 12. Puntos de contacto con el módulo de turnos (futuro)

- El **Cupon** generado acá se redime en el módulo de turnos: **1 cupón por corte**, no acumulable con otros cupones en el mismo turno.
- Validez del cupón: **2 meses** desde su generación.
- El % del cupón se aplica sobre el precio del corte (a definir en la spec de turnos).

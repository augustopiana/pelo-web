# Documentación técnica — Módulo de Vinilos

> **Notas de desarrollo.** Este documento describe el "cómo" (arquitectura e implementación). El "qué" (dominio y reglas de negocio) está en `spec-modulo-vinilos.md`, que es la fuente de verdad del comportamiento. Ante cualquier conflicto, manda la spec de dominio.
> Objetivo: que alguien que retome el proyecto entienda cómo funciona por detrás.

---

## 1. Stack tecnológico

| Capa | Tecnología | Notas |
|------|-----------|-------|
| Backend | **Java + Spring Boot 3.x** | API REST. Java LTS (21 o 25). Pin de versión exacta al iniciar el repo. |
| Frontend | **React** | SPA. Recomendado con Vite + TypeScript. |
| Base de datos | **PostgreSQL** | Relacional; encaja con el modelo de entidades de la spec. |
| ORM | Spring Data JPA / Hibernate | Mapeo de entidades y repositorios. |
| Migraciones | Flyway | Versionado del esquema de BD. |
| Seguridad | Spring Security + JWT | Login email/contraseña y Google OAuth2. |
| Pagos | Mercado Pago (SDK Java + SDK React) | Checkout + webhooks + devoluciones. |
| Email | Spring Mail (SMTP) | Verificación de cuenta y confirmaciones. |
| Scheduler | Spring `@Scheduled` (o Quartz) | Jobs de vencimiento y ocultamiento. |
| Almacenamiento de fotos | Object storage (S3-compatible) o disco + CDN | URLs guardadas en `FotoVinilo`. |
| Build | Maven o Gradle (back), npm/pnpm (front) | |

> Las versiones exactas se fijan al inicializar el proyecto y se registran en `registro-releases.md`. Conviene verificar la última versión estable de cada dependencia en ese momento.

---

## 2. Arquitectura general

```
┌─────────────────┐        HTTPS / REST + JWT        ┌──────────────────────┐
│   Frontend      │ ───────────────────────────────► │   Backend            │
│   React SPA     │ ◄─────────────────────────────── │   Spring Boot        │
│  (catálogo,     │                                   │  (API REST)          │
│   panel dueño,  │                                   │                      │
│   checkout MP)  │                                   │  ┌────────────────┐  │
└───────┬─────────┘                                   │  │ Controllers    │  │
        │                                             │  │ Services       │  │
        │  SDK MP (Bricks/Checkout Pro)               │  │ Repositories   │  │
        ▼                                             │  │ Scheduler/Jobs │  │
┌─────────────────┐        webhooks (pagos)           │  └────────┬───────┘  │
│  Mercado Pago   │ ────────────────────────────────► │           │          │
└─────────────────┘                                   └───────────┼──────────┘
                                                                  ▼
                                    ┌──────────────┐   ┌──────────────────┐
                                    │  PostgreSQL  │   │  Object storage  │
                                    └──────────────┘   │  (fotos)         │
                                    ┌──────────────┐   └──────────────────┘
                                    │  SMTP / Mail │
                                    └──────────────┘
```

Flujo base: el frontend consume la API REST. Los pagos se inician desde el frontend con el SDK de Mercado Pago; **la confirmación real del pago llega al backend por webhook** (nunca se confía solo en el redirect del navegador). Los jobs programados corren dentro del backend.

---

## 3. Backend (Spring Boot)

### 3.1 Estructura de capas
- **Controller** — endpoints REST, validación de entrada (DTOs), sin lógica de negocio.
- **Service** — lógica de negocio y transacciones (`@Transactional`); acá viven las reglas R-1…R-13 de la spec.
- **Repository** — Spring Data JPA.
- **Domain / Entities** — entidades JPA + enums.
- **Integration** — clientes de Mercado Pago, mail, storage.
- **Scheduler** — jobs.

### 3.2 Entidades JPA (mapeo del §4 de la spec)
Se mapean 1:1 con la spec: `Usuario`, `Vinilo`, `FotoVinilo`, `Genero`, `Orden`, `ItemOrden`, `Pago`, `Reembolso`, `Cupon`, `NotificacionDueno`, `PedidoBusqueda`.

Enums a persistir (usar `@Enumerated(EnumType.STRING)` para legibilidad en BD):
- `Vinilo.estado`: `DISPONIBLE`, `VENDIDO`, `PAUSADO`. *(v0.2: se elimina `RESERVADO`.)*
- `Vinilo.estadoDisco` (Goldmine): `MINT`, `NEAR_MINT`, `VG_PLUS_PLUS`, `VG_PLUS`, `VG`, `GOOD`, `POOR`.
- `Vinilo.formato`: `VINILO` (preparado: `CD`, `CASSETTE`).
- `Orden.estado`: `PENDIENTE_PAGO`, `PAGADA`, `ENTREGADA`, `ENVIADA`, `CANCELADA`.
- `Orden.modoEntrega`: `RETIRO`, `ENVIO`.
- `Pago.medio`: `MERCADOPAGO`, `EFECTIVO`; `Pago.estado`: `PENDIENTE`, `APROBADO`, `RECHAZADO`.
- `Cupon.estado`: `ACTIVO`, `USADO`, `VENCIDO`.
- `NotificacionDueno.tipo`: `NUEVA_COMPRA`, `NUEVO_PEDIDO_BUSQUEDA`.
- `PedidoBusqueda.estado`: `BUSCANDO`, `ENCONTRADO`, `NO_ENCONTRADO`.

> **Cambio v0.2 (sin seña):** se eliminan `Vinilo.senable`, `Orden.tipo` (`SENA`/`COMPRA_DIRECTA`), `ItemOrden.estadoItem`, `Pago.tipo` (`SENA`/`RESTO`/`TOTAL`) y el enum `Reembolso.motivo` (pasa a texto libre; devolución excepcional). Se agregan `Orden.modoEntrega`, los datos de envío en `Orden`, y la entidad `PedidoBusqueda`.

### 3.3 Migraciones
Flyway con scripts versionados (`V1__init.sql`, `V2__…`). El esquema nunca se toca a mano en prod; todo cambio va por migración y queda referenciado en el registro de releases.

### 3.4 Autenticación y seguridad
- **Email/contraseña:** hash con BCrypt. Registro genera token de verificación enviado por email; la cuenta no puede operar (señar/comprar) hasta verificar (**R-12**).
- **Google:** OAuth2 login; se vincula por `google_id`.
- **Sesión:** JWT (access + refresh). Endpoints de operación exigen usuario autenticado y verificado.
- **Roles:** `CLIENTE` y `ADMIN`; los endpoints de panel exigen `ADMIN`.

### 3.5 Integración con Mercado Pago
Decisión pendiente menor: **Checkout Pro** (redirect, más simple) vs. **Payment Brick** (checkout embebido, mejor UX). Recomendado empezar con Checkout Pro por simplicidad; el modelo no cambia.

Puntos técnicos clave (v0.2: solo compra directa):
- **Inicio de pago:** al crear la orden (`PENDIENTE_PAGO`), el backend crea una preferencia/pago en MP por el **100% del total** y devuelve al frontend lo necesario para el checkout.
- **Confirmación por webhook:** MP notifica el resultado al backend (endpoint de webhook). **Solo con el webhook aprobado** se pasa la orden a `PAGADA`, se marcan los vinilos `VENDIDO`, se genera el código de retiro (si `modoEntrega = RETIRO`) y (si corresponde) el cupón. El redirect del navegador se usa solo para UX, no como fuente de verdad.
- **Idempotencia:** los webhooks pueden llegar duplicados; procesar de forma idempotente (verificar si el pago ya fue registrado).
- **Devoluciones (excepcionales):** el backend llama a la API de refund de MP. Registrar `mp_refund_id` en `Reembolso`. La compra es venta final; el reembolso es una acción manual del dueño para casos puntuales. (MP no cobra comisión en la devolución y permite hasta 90 días.)
- **Venta walk-in en efectivo:** no pasa por MP; se registra un `Pago` con `medio = EFECTIVO` desde el panel.

### 3.6 Jobs programados
- **Job de ocultamiento de vendidos (Flujo E):** corre diariamente; oculta del catálogo público los vinilos `VENDIDO` con `fecha_venta` > 30 días. No borra el registro; se filtra en las queries del catálogo (ej. flag `visible_publico` o condición por fecha).
- **Generación de cupón:** **no es un job**, es event-driven — se dispara al confirmarse la compra (webhook aprobado), aplicando R-8 (tope de 1 cada 30 días corridos, % = mayor de los ítems de la orden, validez 2 meses).

> **Cambio v0.2:** se elimina el job de vencimiento de reservas (no hay seña/reserva).

### 3.7 Generación del código de retiro (R-13)
- Alfabeto sin ambiguos (excluir `0 O 1 I L`), mayúsculas. Ej. `ABCDEFGHJKMNPQRSTUVWXYZ23456789`.
- Longitud 6–8; formato agrupado para dictado (ej. `H7K-2P9`).
- Generación aleatoria (`SecureRandom`), verificación de unicidad contra códigos vigentes; reintento ante colisión.
- Se genera al confirmarse el pago (webhook aprobado).

### 3.8 Concurrencia (R-11)
Cada vinilo es pieza única: hay que evitar la **doble venta**. Opciones:
- **Bloqueo optimista:** columna `@Version` en `Vinilo`; si dos operaciones compiten, una falla y se reintenta/rechaza.
- **Bloqueo pesimista** (`SELECT … FOR UPDATE`) al momento de crear la orden y cambiar el estado del vinilo.
Recomendado: al crear la orden, verificar en una transacción que el vinilo está `DISPONIBLE` y marcarlo como "comprometido" (para que no lo compre otro mientras se paga); si el pago no se aprueba o expira, liberarlo. Al aprobarse el pago (webhook) pasa a `VENDIDO`.

### 3.9 Notificaciones
- **Al dueño:** registro en `NotificacionDueno` (alimenta el "cartelito"/badge del panel) + email vía Spring Mail. (WhatsApp queda para fase 2.)
- **Al cliente:** email de confirmación con el código de retiro; verificación de cuenta.

---

## 4. Frontend (React)

- **Base:** React + Vite + TypeScript; routing con React Router; estado de servidor con React Query (o similar) para cachear catálogo y órdenes.
- **Autenticación:** manejo de JWT (access/refresh), login email/contraseña y botón de Google.
- **Catálogo público:** grilla con portada, buscador, filtros (artista, género, precio, estado), orden por más nuevos, ficha con galería, descuento en corte y CTA **Comprar**. Espacio **"¿No lo encontrás? Pedilo"** para crear un pedido de búsqueda. Tooltip/glosario de la escala Goldmine (§8 de la spec).
- **Checkout:** elección de **entrega** (retiro / envío con datos de dirección) + integración con el SDK de Mercado Pago.
- **Cuenta del cliente:** órdenes y estados, vinilos comprados, cupones, y **mis pedidos de búsqueda** con su estado.
- **Panel del dueño:** dashboard, ABM de vinilos y fotos, órdenes (retiro: ingresar código y confirmar entrega; envío: ver dirección y marcar despachado), **pedidos de búsqueda** (marcar encontrado/no encontrado, ver contacto), venta walk-in, reembolsos.

---

## 5. API REST (endpoints principales)

> Borrador orientativo; se ajusta al implementar.

| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| POST | `/auth/register` | público | Registro (dispara verificación por email) |
| POST | `/auth/login` | público | Login email/contraseña |
| GET | `/auth/google` | público | Login con Google |
| GET | `/auth/verify` | público | Verificación de email por token |
| GET | `/vinilos` | público | Catálogo con búsqueda/filtros/orden |
| GET | `/vinilos/{id}` | público | Ficha |
| POST | `/vinilos` | admin | Alta de vinilo |
| PUT | `/vinilos/{id}` | admin | Edición |
| PATCH | `/vinilos/{id}/pausar` | admin | Pausar/reactivar |
| POST | `/vinilos/{id}/venta-efectivo` | admin | Venta walk-in (Flujo D) |
| POST | `/ordenes` | cliente | Crear orden de compra directa (uno o varios vinilos) + modo de entrega |
| GET | `/ordenes/mias` | cliente | Órdenes del cliente |
| POST | `/webhooks/mercadopago` | MP | Confirmación de pagos/refunds |
| POST | `/retiros/{codigo}` | admin | Buscar orden por código (retiro) |
| POST | `/retiros/{codigo}/entregar` | admin | Confirmar entrega en el local (Flujo B) |
| POST | `/admin/ordenes/{id}/despachar` | admin | Marcar orden como enviada por correo (Flujo C) |
| POST | `/pedidos-busqueda` | cliente | Crear pedido de búsqueda (R-15) |
| GET | `/pedidos-busqueda/mios` | cliente | Mis pedidos de búsqueda |
| GET | `/admin/pedidos-busqueda` | admin | Pedidos de búsqueda (abiertos/resueltos) |
| POST | `/admin/pedidos-busqueda/{id}/resolver` | admin | Marcar encontrado / no encontrado |
| GET | `/panel/dashboard` | admin | Métricas del dashboard |
| GET | `/cupones/mios` | cliente | Cupones del cliente |

> **Cambio v0.2:** se eliminan `/ordenes/sena`, `/ordenes/{id}/cancelar` y la resolución por ítem del retiro (`/retiros/.../vender`, `/rechazar`). Se agregan el despacho de envíos y los endpoints de pedidos de búsqueda.

---

## 6. Entornos, despliegue y configuración

- **Entornos:** `dev` (local), `prod`. Perfiles de Spring (`application-dev.yml`, `application-prod.yml`).
- **Secrets:** credenciales de MP, SMTP, BD y JWT nunca en el repo; usar variables de entorno / gestor de secretos.
- **Hosting:** a definir (D pendiente). El backend necesita ejecución continua (por los webhooks y los jobs), así que un hosting siempre-activo, no serverless efímero, simplifica los jobs programados.
- **Webhooks:** el endpoint de MP debe ser accesible públicamente por HTTPS (en dev, usar un túnel tipo ngrok).

---

## 7. Testing (mínimos recomendados)
- **Unitarios** de servicios: reglas R-1…R-13, en especial cupones (R-8), estados de orden/ítem y devoluciones.
- **Integración:** flujos de pago con MP en sandbox (webhooks incluidos), concurrencia de doble reserva (R-11).
- **E2E** del frontend para los flujos críticos (señar, comprar, retirar/rechazar).

---

## 8. Convenciones y seguimiento
- **Versionado:** SemVer. Cada iteración se anota en `registro-releases.md` (qué se hizo, migraciones incluidas, desde dónde seguir).
- **Backlog:** las historias de usuario y tasks se marcan como hechas en `historias-usuario.md`.
- **Commits/branches:** a definir (sugerido: Conventional Commits + trunk o feature branches).

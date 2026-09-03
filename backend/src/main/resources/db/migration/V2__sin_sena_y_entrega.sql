-- V2__sin_sena_y_entrega.sql
-- Cambio de alcance spec v0.2: se elimina la seña/reserva. Solo compra directa.
-- Se agrega modo de entrega (retiro/envío) + datos de envío, y un hold temporal
-- de checkout en el vinilo (R-11). Fuente de verdad: spec-modulo-vinilos.md (v0.2).

-- ---------------------------------------------------------------------------
-- Vinilo: sin `senable`, sin estado RESERVADO; hold temporal para el checkout.
-- ---------------------------------------------------------------------------
UPDATE vinilo SET estado = 'DISPONIBLE' WHERE estado = 'RESERVADO';

ALTER TABLE vinilo DROP CONSTRAINT ck_vinilo_estado;
ALTER TABLE vinilo ADD CONSTRAINT ck_vinilo_estado CHECK (estado IN
    ('DISPONIBLE', 'VENDIDO', 'PAUSADO'));

ALTER TABLE vinilo DROP COLUMN senable;

-- Bloqueo temporal mientras un cliente está pagando (se libera si no se aprueba).
ALTER TABLE vinilo ADD COLUMN bloqueo_hasta TIMESTAMPTZ;

-- ---------------------------------------------------------------------------
-- Orden: sin `tipo` ni reserva; estados de compra directa; modo de entrega y envío.
-- ---------------------------------------------------------------------------
ALTER TABLE orden DROP CONSTRAINT ck_orden_tipo;
ALTER TABLE orden DROP COLUMN tipo;
ALTER TABLE orden DROP COLUMN fecha_vencimiento;
ALTER TABLE orden DROP COLUMN fecha_cierre;

ALTER TABLE orden DROP CONSTRAINT ck_orden_estado;
ALTER TABLE orden ADD CONSTRAINT ck_orden_estado CHECK (estado IN
    ('PENDIENTE_PAGO', 'PAGADA', 'ENTREGADA', 'ENVIADA', 'CANCELADA'));

ALTER TABLE orden ADD COLUMN modo_entrega VARCHAR(255);
ALTER TABLE orden ADD CONSTRAINT ck_orden_modo_entrega CHECK (modo_entrega IN ('RETIRO', 'ENVIO'));
ALTER TABLE orden ADD COLUMN fecha_pago TIMESTAMPTZ;
ALTER TABLE orden ADD COLUMN fecha_entrega TIMESTAMPTZ;
ALTER TABLE orden ADD COLUMN fecha_despacho TIMESTAMPTZ;

-- Datos de envío (solo modo_entrega = ENVIO)
ALTER TABLE orden ADD COLUMN envio_nombre VARCHAR(255);
ALTER TABLE orden ADD COLUMN envio_telefono VARCHAR(255);
ALTER TABLE orden ADD COLUMN envio_direccion VARCHAR(255);
ALTER TABLE orden ADD COLUMN envio_localidad VARCHAR(255);
ALTER TABLE orden ADD COLUMN envio_provincia VARCHAR(255);
ALTER TABLE orden ADD COLUMN envio_cp VARCHAR(255);

-- ---------------------------------------------------------------------------
-- ItemOrden: sin resolución ítem por ítem (era de la seña).
-- ---------------------------------------------------------------------------
ALTER TABLE item_orden DROP CONSTRAINT ck_item_estado;
ALTER TABLE item_orden DROP CONSTRAINT ck_item_metodo_resto;
ALTER TABLE item_orden DROP COLUMN estado_item;
ALTER TABLE item_orden DROP COLUMN resto_pagado;
ALTER TABLE item_orden DROP COLUMN metodo_resto;

-- ---------------------------------------------------------------------------
-- Pago: sin `tipo` (siempre es el pago total).
-- ---------------------------------------------------------------------------
ALTER TABLE pago DROP CONSTRAINT ck_pago_tipo;
ALTER TABLE pago DROP COLUMN tipo;

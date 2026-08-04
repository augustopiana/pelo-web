-- V1__init.sql
-- Esquema inicial del modulo de vinilos.
-- Mapea 1:1 el modelo de dominio de la spec (§4) y persiste los enums como
-- varchar con CHECK (EnumType.STRING, legible en BD). Fuente de verdad: spec-modulo-vinilos.md.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------------
-- Usuario (spec 4.1)
-- ---------------------------------------------------------------------------
CREATE TABLE usuario (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre            VARCHAR(255) NOT NULL,
    email             VARCHAR(255) NOT NULL,
    email_verificado  BOOLEAN      NOT NULL DEFAULT FALSE,
    password_hash     VARCHAR(255),
    google_id         VARCHAR(255),
    telefono          VARCHAR(255),
    rol               VARCHAR(255) NOT NULL DEFAULT 'CLIENTE',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('CLIENTE', 'ADMIN'))
);

-- ---------------------------------------------------------------------------
-- Genero (spec 4.4)
-- ---------------------------------------------------------------------------
CREATE TABLE genero (
    id      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre  VARCHAR(255) NOT NULL,
    CONSTRAINT uq_genero_nombre UNIQUE (nombre)
);

-- ---------------------------------------------------------------------------
-- Vinilo (spec 4.2, 5.1). Pieza unica; columna version para bloqueo optimista (R-11).
-- ---------------------------------------------------------------------------
CREATE TABLE vinilo (
    id                   UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo               VARCHAR(255)  NOT NULL,
    artista              VARCHAR(255)  NOT NULL,
    genero_id            UUID,
    anio                 INTEGER,
    sello                VARCHAR(255),
    edicion_pais         VARCHAR(255),
    formato              VARCHAR(255)  NOT NULL DEFAULT 'VINILO',
    estado_disco         VARCHAR(255)  NOT NULL,
    descripcion          TEXT,
    precio               NUMERIC(12,2) NOT NULL,
    descuento_corte_pct  INTEGER       NOT NULL DEFAULT 0,
    senable              BOOLEAN       NOT NULL,
    estado               VARCHAR(255)  NOT NULL DEFAULT 'DISPONIBLE',
    fecha_publicacion    TIMESTAMPTZ   NOT NULL,
    fecha_venta          TIMESTAMPTZ,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version              BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT fk_vinilo_genero FOREIGN KEY (genero_id) REFERENCES genero (id),
    CONSTRAINT ck_vinilo_formato CHECK (formato IN ('VINILO', 'CD', 'CASSETTE')),
    CONSTRAINT ck_vinilo_estado_disco CHECK (estado_disco IN
        ('MINT', 'NEAR_MINT', 'VG_PLUS_PLUS', 'VG_PLUS', 'VG', 'GOOD', 'POOR')),
    CONSTRAINT ck_vinilo_estado CHECK (estado IN
        ('DISPONIBLE', 'RESERVADO', 'VENDIDO', 'PAUSADO'))
);

CREATE INDEX ix_vinilo_estado ON vinilo (estado);
CREATE INDEX ix_vinilo_fecha_publicacion ON vinilo (fecha_publicacion);
CREATE INDEX ix_vinilo_genero_id ON vinilo (genero_id);

-- ---------------------------------------------------------------------------
-- FotoVinilo (spec 4.3)
-- ---------------------------------------------------------------------------
CREATE TABLE foto_vinilo (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    vinilo_id   UUID         NOT NULL,
    url         VARCHAR(255) NOT NULL,
    orden       INTEGER      NOT NULL,
    es_portada  BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_foto_vinilo FOREIGN KEY (vinilo_id) REFERENCES vinilo (id)
);

CREATE INDEX ix_foto_vinilo_vinilo_id ON foto_vinilo (vinilo_id);

-- ---------------------------------------------------------------------------
-- Orden (spec 4.5, 5.2). No mezcla senables y no senables (R-10).
-- ---------------------------------------------------------------------------
CREATE TABLE orden (
    id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id         UUID          NOT NULL,
    tipo               VARCHAR(255)  NOT NULL,
    estado             VARCHAR(255)  NOT NULL,
    total              NUMERIC(12,2) NOT NULL,
    monto_pagado       NUMERIC(12,2),
    codigo_retiro      VARCHAR(255),
    fecha_vencimiento  TIMESTAMPTZ,
    fecha_cierre       TIMESTAMPTZ,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_orden_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT uq_orden_codigo_retiro UNIQUE (codigo_retiro),
    CONSTRAINT ck_orden_tipo CHECK (tipo IN ('SENA', 'COMPRA_DIRECTA')),
    CONSTRAINT ck_orden_estado CHECK (estado IN
        ('PENDIENTE_PAGO', 'ACTIVA', 'CERRADA', 'CANCELADA', 'VENCIDA', 'PAGADA', 'ENTREGADA'))
);

CREATE INDEX ix_orden_usuario_id ON orden (usuario_id);
CREATE INDEX ix_orden_estado ON orden (estado);

-- ---------------------------------------------------------------------------
-- ItemOrden (spec 4.6)
-- ---------------------------------------------------------------------------
CREATE TABLE item_orden (
    id                   UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id             UUID          NOT NULL,
    vinilo_id            UUID          NOT NULL,
    precio               NUMERIC(12,2) NOT NULL,
    descuento_corte_pct  INTEGER       NOT NULL,
    estado_item          VARCHAR(255)  NOT NULL DEFAULT 'PENDIENTE',
    resto_pagado         BOOLEAN       NOT NULL DEFAULT FALSE,
    metodo_resto         VARCHAR(255),
    CONSTRAINT fk_item_orden_orden FOREIGN KEY (orden_id) REFERENCES orden (id),
    CONSTRAINT fk_item_orden_vinilo FOREIGN KEY (vinilo_id) REFERENCES vinilo (id),
    CONSTRAINT ck_item_estado CHECK (estado_item IN
        ('PENDIENTE', 'VENDIDO', 'RECHAZADO', 'VENCIDO')),
    CONSTRAINT ck_item_metodo_resto CHECK (metodo_resto IS NULL OR metodo_resto IN
        ('EFECTIVO', 'ONLINE'))
);

CREATE INDEX ix_item_orden_orden_id ON item_orden (orden_id);
CREATE INDEX ix_item_orden_vinilo_id ON item_orden (vinilo_id);

-- ---------------------------------------------------------------------------
-- Pago (spec 4.7). El aprobado se confirma via webhook de MP.
-- ---------------------------------------------------------------------------
CREATE TABLE pago (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id       UUID          NOT NULL,
    tipo           VARCHAR(255)  NOT NULL,
    monto          NUMERIC(12,2) NOT NULL,
    medio          VARCHAR(255)  NOT NULL,
    mp_payment_id  VARCHAR(255),
    estado         VARCHAR(255)  NOT NULL DEFAULT 'PENDIENTE',
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_pago_orden FOREIGN KEY (orden_id) REFERENCES orden (id),
    CONSTRAINT ck_pago_tipo CHECK (tipo IN ('SENA', 'RESTO', 'TOTAL')),
    CONSTRAINT ck_pago_medio CHECK (medio IN ('MERCADOPAGO', 'EFECTIVO')),
    CONSTRAINT ck_pago_estado CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
);

CREATE INDEX ix_pago_orden_id ON pago (orden_id);
CREATE INDEX ix_pago_mp_payment_id ON pago (mp_payment_id);

-- ---------------------------------------------------------------------------
-- Reembolso (spec 4.8). Devolucion por item; motivo R-3 / R-4.
-- ---------------------------------------------------------------------------
CREATE TABLE reembolso (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    pago_id       UUID          NOT NULL,
    item_id       UUID          NOT NULL,
    monto         NUMERIC(12,2) NOT NULL,
    mp_refund_id  VARCHAR(255),
    motivo        VARCHAR(255)  NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_reembolso_pago FOREIGN KEY (pago_id) REFERENCES pago (id),
    CONSTRAINT fk_reembolso_item FOREIGN KEY (item_id) REFERENCES item_orden (id),
    CONSTRAINT ck_reembolso_motivo CHECK (motivo IN
        ('RECHAZO_PRUEBA', 'CANCELACION_VOLUNTARIA'))
);

CREATE INDEX ix_reembolso_pago_id ON reembolso (pago_id);
CREATE INDEX ix_reembolso_item_id ON reembolso (item_id);

-- ---------------------------------------------------------------------------
-- Cupon (spec 4.9, R-8)
-- ---------------------------------------------------------------------------
CREATE TABLE cupon (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id         UUID         NOT NULL,
    orden_id           UUID         NOT NULL,
    porcentaje         INTEGER      NOT NULL,
    fecha_generacion   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    fecha_vencimiento  TIMESTAMPTZ  NOT NULL,
    estado             VARCHAR(255) NOT NULL DEFAULT 'ACTIVO',
    fecha_uso          TIMESTAMPTZ,
    CONSTRAINT fk_cupon_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_cupon_orden FOREIGN KEY (orden_id) REFERENCES orden (id),
    CONSTRAINT ck_cupon_estado CHECK (estado IN ('ACTIVO', 'USADO', 'VENCIDO'))
);

CREATE INDEX ix_cupon_usuario_id ON cupon (usuario_id);

-- ---------------------------------------------------------------------------
-- NotificacionDueno (spec 4.10)
-- ---------------------------------------------------------------------------
CREATE TABLE notificacion_dueno (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo        VARCHAR(255) NOT NULL,
    orden_id    UUID,
    leida       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_notificacion_orden FOREIGN KEY (orden_id) REFERENCES orden (id),
    CONSTRAINT ck_notificacion_tipo CHECK (tipo IN
        ('NUEVA_SENA', 'NUEVA_COMPRA', 'RESERVA_POR_VENCER'))
);

CREATE INDEX ix_notificacion_leida ON notificacion_dueno (leida);

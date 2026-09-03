package com.peloweb.vinilos.domain.enums;

/**
 * Estados de orden (spec 5.2, v0.2 compra directa):
 * PENDIENTE_PAGO -> PAGADA -> ENTREGADA (retiro) / ENVIADA (envío); o CANCELADA (pago rechazado).
 */
public enum EstadoOrden {
    PENDIENTE_PAGO,
    PAGADA,
    ENTREGADA,
    ENVIADA,
    CANCELADA
}

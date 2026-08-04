package com.peloweb.vinilos.domain.enums;

/**
 * Estados de orden (spec 5.2). Union de ambas maquinas:
 * - Sena: PENDIENTE_PAGO -> ACTIVA -> CERRADA / CANCELADA / VENCIDA.
 * - Compra directa: PENDIENTE_PAGO -> PAGADA -> ENTREGADA / CANCELADA.
 */
public enum EstadoOrden {
    PENDIENTE_PAGO,
    ACTIVA,
    CERRADA,
    CANCELADA,
    VENCIDA,
    PAGADA,
    ENTREGADA
}

package com.peloweb.vinilos.domain.enums;

/** Estado de un pago (spec 4.7). El aprobado se confirma via webhook de MP. */
public enum EstadoPago {
    PENDIENTE,
    APROBADO,
    RECHAZADO
}

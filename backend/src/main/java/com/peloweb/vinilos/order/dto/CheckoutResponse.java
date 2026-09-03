package com.peloweb.vinilos.order.dto;

import java.util.UUID;

/** Respuesta al crear una orden: a dónde mandar al cliente a pagar. */
public record CheckoutResponse(UUID ordenId, String checkoutUrl) {
}

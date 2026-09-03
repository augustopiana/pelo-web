package com.peloweb.vinilos.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Un vinilo dentro de una orden (vista del panel). */
public record ItemLineaDTO(UUID viniloId, String titulo, String artista, BigDecimal precio) {
}

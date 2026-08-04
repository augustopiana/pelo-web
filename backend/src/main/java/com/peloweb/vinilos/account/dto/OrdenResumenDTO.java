package com.peloweb.vinilos.account.dto;

import com.peloweb.vinilos.domain.Orden;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenResumenDTO(
        UUID id,
        String tipo,
        String estado,
        BigDecimal total,
        BigDecimal montoPagado,
        String codigoRetiro,
        OffsetDateTime fechaVencimiento,
        OffsetDateTime createdAt) {

    public static OrdenResumenDTO from(Orden o) {
        return new OrdenResumenDTO(o.getId(), o.getTipo().name(), o.getEstado().name(),
                o.getTotal(), o.getMontoPagado(), o.getCodigoRetiro(),
                o.getFechaVencimiento(), o.getCreatedAt());
    }
}

package com.peloweb.vinilos.account.dto;

import com.peloweb.vinilos.domain.Orden;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenResumenDTO(
        UUID id,
        String estado,
        String modoEntrega,
        BigDecimal total,
        String codigoRetiro,
        OffsetDateTime createdAt) {

    public static OrdenResumenDTO from(Orden o) {
        return new OrdenResumenDTO(o.getId(), o.getEstado().name(), o.getModoEntrega().name(),
                o.getTotal(), o.getCodigoRetiro(), o.getCreatedAt());
    }
}

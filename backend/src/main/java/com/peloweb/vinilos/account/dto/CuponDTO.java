package com.peloweb.vinilos.account.dto;

import com.peloweb.vinilos.domain.Cupon;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CuponDTO(
        UUID id,
        Integer porcentaje,
        String estado,
        OffsetDateTime fechaGeneracion,
        OffsetDateTime fechaVencimiento) {

    public static CuponDTO from(Cupon c) {
        return new CuponDTO(c.getId(), c.getPorcentaje(), c.getEstado().name(),
                c.getFechaGeneracion(), c.getFechaVencimiento());
    }
}

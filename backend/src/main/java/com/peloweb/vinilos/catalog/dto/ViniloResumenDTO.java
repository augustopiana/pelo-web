package com.peloweb.vinilos.catalog.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Vinilo para la grilla del catalogo (spec §8). */
public record ViniloResumenDTO(
        UUID id,
        String titulo,
        String artista,
        String genero,
        Integer anio,
        BigDecimal precio,
        String estadoDisco,
        String estado,
        boolean senable,
        Integer descuentoCortePct,
        String portadaUrl,
        OffsetDateTime fechaPublicacion) {
}

package com.peloweb.vinilos.catalog.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Ficha completa del vinilo (spec §8). */
public record ViniloDetalleDTO(
        UUID id,
        String titulo,
        String artista,
        GeneroDTO genero,
        Integer anio,
        String sello,
        String edicionPais,
        String formato,
        String estadoDisco,
        String descripcion,
        BigDecimal precio,
        Integer descuentoCortePct,
        boolean senable,
        String estado,
        OffsetDateTime fechaPublicacion,
        List<FotoDTO> fotos) {
}

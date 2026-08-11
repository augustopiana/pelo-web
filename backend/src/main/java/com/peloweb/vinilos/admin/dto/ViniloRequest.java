package com.peloweb.vinilos.admin.dto;

import com.peloweb.vinilos.domain.enums.EstadoDisco;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/** Datos editables de un vinilo por el dueno (alta y edicion). */
public record ViniloRequest(
        @NotBlank String titulo,
        @NotBlank String artista,
        UUID generoId,
        Integer anio,
        String sello,
        String edicionPais,
        @NotNull EstadoDisco estadoDisco,
        String descripcion,
        @NotNull @DecimalMin("0.0") BigDecimal precio,
        @NotNull @Min(0) @Max(100) Integer descuentoCortePct,
        @NotNull Boolean senable) {
}

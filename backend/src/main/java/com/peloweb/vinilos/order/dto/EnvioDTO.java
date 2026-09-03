package com.peloweb.vinilos.order.dto;

import jakarta.validation.constraints.NotBlank;

/** Datos de envío (requeridos si el modo de entrega es ENVIO). */
public record EnvioDTO(
        @NotBlank String nombre,
        @NotBlank String telefono,
        @NotBlank String direccion,
        @NotBlank String localidad,
        @NotBlank String provincia,
        @NotBlank String cp) {
}

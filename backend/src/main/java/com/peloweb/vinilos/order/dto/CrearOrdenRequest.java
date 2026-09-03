package com.peloweb.vinilos.order.dto;

import com.peloweb.vinilos.domain.enums.ModoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** Alta de una orden de compra directa. */
public record CrearOrdenRequest(
        @NotEmpty List<UUID> viniloIds,
        @NotNull ModoEntrega modoEntrega,
        @Valid EnvioDTO envio) {
}

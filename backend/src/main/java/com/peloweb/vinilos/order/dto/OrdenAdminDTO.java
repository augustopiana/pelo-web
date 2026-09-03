package com.peloweb.vinilos.order.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Orden vista desde el panel del dueño (con cliente, envío e ítems). */
public record OrdenAdminDTO(
        UUID id,
        String estado,
        String modoEntrega,
        BigDecimal total,
        String codigoRetiro,
        OffsetDateTime createdAt,
        OffsetDateTime fechaPago,
        OffsetDateTime fechaEntrega,
        OffsetDateTime fechaDespacho,
        String clienteNombre,
        String clienteEmail,
        String clienteTelefono,
        EnvioDTO envio,
        List<ItemLineaDTO> items) {
}

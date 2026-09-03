package com.peloweb.vinilos.order;

import com.peloweb.vinilos.auth.dto.MessageResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Simulación del pago para desarrollo (reemplaza el webhook real de MP).
 * Solo activo en perfil dev. Dispara la MISMA confirmación idempotente que el webhook.
 */
@RestController
@Profile("dev")
public class DevPagoController {

    private final OrdenService ordenes;

    public DevPagoController(OrdenService ordenes) {
        this.ordenes = ordenes;
    }

    @PostMapping("/dev/pagos/simular")
    public MessageResponse simular(@RequestParam UUID orden,
                                   @RequestParam(defaultValue = "true") boolean aprobado) {
        String referencia = ordenes.referenciaPagoDeOrden(orden);
        ordenes.confirmarPago(referencia, aprobado);
        return new MessageResponse(aprobado ? "Pago aprobado (simulado)" : "Pago rechazado (simulado)");
    }
}

package com.peloweb.vinilos.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Webhook de confirmación de pagos. En modo dev, el pago se confirma vía DevPagoController;
 * en modo mercadopago, MP notifica acá y se consulta el estado real del pago.
 * La confirmación es idempotente (OrdenService.confirmarPago).
 *
 * NOTA: el cuerpo real de MP trae {type, data.id}; la implementación completa (consultar
 * el pago a MP y mapear su estado) se agrega al cablear Mercado Pago real.
 */
@RestController
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @PostMapping("/webhooks/mercadopago")
    public ResponseEntity<Void> mercadopago(@RequestBody(required = false) Map<String, Object> body) {
        // Responder 200 siempre para que MP no reintente indefinidamente.
        log.info("[webhook] notificación MP recibida: {}", body);
        // TODO (MP real): extraer data.id, consultar el pago a MP y llamar a
        // ordenService.confirmarPago(referencia, aprobado) de forma idempotente.
        return ResponseEntity.ok().build();
    }
}

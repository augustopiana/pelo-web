package com.peloweb.vinilos.payment;

import com.peloweb.vinilos.domain.Orden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Gateway de desarrollo: no cobra nada. Genera una referencia de pago y una URL que
 * lleva a una página de checkout simulado en el frontend, donde se puede "aprobar" o
 * "rechazar" el pago (que dispara la misma confirmación que el webhook real).
 * Activo con app.payments.mode=dev (default).
 */
@Component
@ConditionalOnProperty(name = "app.payments.mode", havingValue = "dev", matchIfMissing = true)
public class DevPaymentGateway implements PaymentGateway {

    private final String frontendUrl;

    public DevPaymentGateway(@Value("${app.frontend-url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public CheckoutInit iniciarCheckout(Orden orden) {
        String referencia = "dev-" + UUID.randomUUID();
        String checkoutUrl = frontendUrl + "/checkout/simular?orden=" + orden.getId();
        return new CheckoutInit(checkoutUrl, referencia);
    }
}

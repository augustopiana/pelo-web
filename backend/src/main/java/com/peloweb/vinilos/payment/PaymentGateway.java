package com.peloweb.vinilos.payment;

import com.peloweb.vinilos.domain.Orden;

/**
 * Pasarela de pago. Abstrae el checkout para poder cambiar entre un gateway de dev
 * (simulado) y Mercado Pago real sin tocar la lógica de negocio.
 */
public interface PaymentGateway {

    /**
     * Inicia el checkout de una orden. Devuelve la URL a la que mandar al cliente
     * y una referencia del pago en la pasarela (se guarda en Pago.mpPaymentId).
     */
    CheckoutInit iniciarCheckout(Orden orden);

    /** Datos que devuelve la pasarela al iniciar el checkout. */
    record CheckoutInit(String checkoutUrl, String referenciaPago) {
    }
}

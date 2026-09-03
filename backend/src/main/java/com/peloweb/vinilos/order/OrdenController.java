package com.peloweb.vinilos.order;

import com.peloweb.vinilos.order.dto.CheckoutResponse;
import com.peloweb.vinilos.order.dto.CrearOrdenRequest;
import com.peloweb.vinilos.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Compra directa (E5): el cliente crea la orden y arranca el checkout. */
@RestController
public class OrdenController {

    private final OrdenService ordenes;

    public OrdenController(OrdenService ordenes) {
        this.ordenes = ordenes;
    }

    @PostMapping("/ordenes")
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse crear(@AuthenticationPrincipal AuthUser principal,
                                  @Valid @RequestBody CrearOrdenRequest req) {
        return ordenes.crear(principal, req);
    }
}

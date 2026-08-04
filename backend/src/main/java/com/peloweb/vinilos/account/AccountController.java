package com.peloweb.vinilos.account;

import com.peloweb.vinilos.account.dto.CuponDTO;
import com.peloweb.vinilos.account.dto.OrdenResumenDTO;
import com.peloweb.vinilos.security.AuthUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Datos de la cuenta del cliente autenticado (HU-03 / T-08).
 * En M1 no hay ordenes ni cupones todavia; devuelven listas vacias.
 */
@RestController
public class AccountController {

    private final OrdenRepository ordenes;
    private final CuponRepository cupones;

    public AccountController(OrdenRepository ordenes, CuponRepository cupones) {
        this.ordenes = ordenes;
        this.cupones = cupones;
    }

    @GetMapping("/ordenes/mias")
    public List<OrdenResumenDTO> misOrdenes(@AuthenticationPrincipal AuthUser principal) {
        return ordenes.findByUsuario_IdOrderByCreatedAtDesc(principal.id()).stream()
                .map(OrdenResumenDTO::from)
                .toList();
    }

    @GetMapping("/cupones/mios")
    public List<CuponDTO> misCupones(@AuthenticationPrincipal AuthUser principal) {
        return cupones.findByUsuario_IdOrderByFechaGeneracionDesc(principal.id()).stream()
                .map(CuponDTO::from)
                .toList();
    }
}

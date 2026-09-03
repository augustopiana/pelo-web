package com.peloweb.vinilos.order;

import com.peloweb.vinilos.catalog.dto.PageResponse;
import com.peloweb.vinilos.order.dto.OrdenAdminDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Gestión de órdenes del dueño (E6). Todo exige ROLE_ADMIN (ver SecurityConfig). */
@RestController
public class AdminOrdenController {

    private final AdminOrdenService service;

    public AdminOrdenController(AdminOrdenService service) {
        this.service = service;
    }

    @GetMapping("/admin/ordenes")
    public PageResponse<OrdenAdminDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listar(page, size);
    }

    /** Buscar una orden por su código de retiro. */
    @GetMapping("/retiros/{codigo}")
    public OrdenAdminDTO buscar(@PathVariable String codigo) {
        return service.buscarPorCodigo(codigo);
    }

    /** Confirmar la entrega en el local (retiro). */
    @PostMapping("/retiros/{codigo}/entregar")
    public OrdenAdminDTO entregar(@PathVariable String codigo) {
        return service.entregar(codigo);
    }

    /** Marcar una orden de envío como despachada. */
    @PostMapping("/admin/ordenes/{id}/despachar")
    public OrdenAdminDTO despachar(@PathVariable UUID id) {
        return service.despachar(id);
    }
}

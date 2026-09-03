package com.peloweb.vinilos.admin;

import com.peloweb.vinilos.admin.dto.ViniloRequest;
import com.peloweb.vinilos.catalog.dto.FotoDTO;
import com.peloweb.vinilos.catalog.dto.PageResponse;
import com.peloweb.vinilos.catalog.dto.ViniloDetalleDTO;
import com.peloweb.vinilos.catalog.dto.ViniloResumenDTO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/** Gestion de vinilos por el dueno (E2). Todo exige ROLE_ADMIN (ver SecurityConfig). */
@RestController
public class AdminViniloController {

    private final AdminViniloService service;

    public AdminViniloController(AdminViniloService service) {
        this.service = service;
    }

    /** Lista TODOS los vinilos (incluye pausados/vendidos), para el panel. */
    @GetMapping("/admin/vinilos")
    public PageResponse<ViniloResumenDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listar(page, size);
    }

    /** Ficha admin de un vinilo (incluye pausados; para el formulario de edicion). */
    @GetMapping("/admin/vinilos/{id}")
    public ViniloDetalleDTO detalle(@PathVariable UUID id) {
        return service.detalle(id);
    }

    @PostMapping("/vinilos")
    @ResponseStatus(HttpStatus.CREATED)
    public ViniloDetalleDTO crear(@Valid @RequestBody ViniloRequest req) {
        return service.crear(req);
    }

    @PutMapping("/vinilos/{id}")
    public ViniloDetalleDTO actualizar(@PathVariable UUID id, @Valid @RequestBody ViniloRequest req) {
        return service.actualizar(id, req);
    }

    @PatchMapping("/vinilos/{id}/pausar")
    public ViniloDetalleDTO pausar(@PathVariable UUID id) {
        return service.togglePausa(id);
    }

    /** Venta en efectivo walk-in (Flujo D). */
    @PostMapping("/vinilos/{id}/venta-efectivo")
    public ViniloDetalleDTO ventaEfectivo(@PathVariable UUID id) {
        return service.ventaEfectivo(id);
    }

    @PostMapping(value = "/vinilos/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<FotoDTO> subirFotos(@PathVariable UUID id, @RequestParam("files") MultipartFile[] files) {
        return service.subirFotos(id, files);
    }

    @DeleteMapping("/vinilos/{id}/fotos/{fotoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarFoto(@PathVariable UUID id, @PathVariable UUID fotoId) {
        service.borrarFoto(id, fotoId);
    }
}

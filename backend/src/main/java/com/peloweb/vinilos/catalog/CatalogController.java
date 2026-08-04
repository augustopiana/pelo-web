package com.peloweb.vinilos.catalog;

import com.peloweb.vinilos.catalog.dto.GeneroDTO;
import com.peloweb.vinilos.catalog.dto.PageResponse;
import com.peloweb.vinilos.catalog.dto.ViniloDetalleDTO;
import com.peloweb.vinilos.catalog.dto.ViniloResumenDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Catalogo publico de solo lectura (spec §8). Endpoints abiertos (ver SecurityConfig). */
@RestController
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/vinilos")
    public PageResponse<ViniloResumenDTO> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String artista,
            @RequestParam(required = false) UUID generoId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String estadoDisco,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return catalog.buscar(q, artista, generoId, precioMin, precioMax, estadoDisco, page, size);
    }

    @GetMapping("/vinilos/{id}")
    public ViniloDetalleDTO ficha(@PathVariable UUID id) {
        return catalog.ficha(id);
    }

    @GetMapping("/generos")
    public List<GeneroDTO> generos() {
        return catalog.generos();
    }
}

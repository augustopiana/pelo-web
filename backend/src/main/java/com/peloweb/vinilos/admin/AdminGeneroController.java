package com.peloweb.vinilos.admin;

import com.peloweb.vinilos.admin.dto.GeneroCreateRequest;
import com.peloweb.vinilos.catalog.GeneroRepository;
import com.peloweb.vinilos.catalog.dto.GeneroDTO;
import com.peloweb.vinilos.domain.Genero;
import com.peloweb.vinilos.web.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Alta de géneros por el dueno (E2 / HU-06). Exige ROLE_ADMIN. */
@RestController
public class AdminGeneroController {

    private final GeneroRepository generos;

    public AdminGeneroController(GeneroRepository generos) {
        this.generos = generos;
    }

    @PostMapping("/generos")
    @ResponseStatus(HttpStatus.CREATED)
    public GeneroDTO crear(@Valid @RequestBody GeneroCreateRequest req) {
        String nombre = req.nombre().trim();
        if (generos.existsByNombreIgnoreCase(nombre)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un género con ese nombre");
        }
        Genero g = new Genero();
        g.setId(UUID.randomUUID());
        g.setNombre(nombre);
        return GeneroDTO.from(generos.save(g));
    }
}

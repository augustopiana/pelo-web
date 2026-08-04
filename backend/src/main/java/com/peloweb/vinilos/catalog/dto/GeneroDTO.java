package com.peloweb.vinilos.catalog.dto;

import com.peloweb.vinilos.domain.Genero;

import java.util.UUID;

public record GeneroDTO(UUID id, String nombre) {

    public static GeneroDTO from(Genero g) {
        return g == null ? null : new GeneroDTO(g.getId(), g.getNombre());
    }
}

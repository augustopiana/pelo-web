package com.peloweb.vinilos.catalog.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Envoltorio de paginacion para las respuestas del catalogo. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PageResponse<T> of(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages());
    }
}

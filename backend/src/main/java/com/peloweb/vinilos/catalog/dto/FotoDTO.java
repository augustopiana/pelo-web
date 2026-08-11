package com.peloweb.vinilos.catalog.dto;

import com.peloweb.vinilos.domain.FotoVinilo;

import java.util.UUID;

public record FotoDTO(UUID id, String url, Integer orden, boolean esPortada) {

    public static FotoDTO from(FotoVinilo f) {
        return new FotoDTO(f.getId(), f.getUrl(), f.getOrden(), f.isEsPortada());
    }
}

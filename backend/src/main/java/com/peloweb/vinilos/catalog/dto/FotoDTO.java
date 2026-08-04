package com.peloweb.vinilos.catalog.dto;

import com.peloweb.vinilos.domain.FotoVinilo;

public record FotoDTO(String url, Integer orden, boolean esPortada) {

    public static FotoDTO from(FotoVinilo f) {
        return new FotoDTO(f.getUrl(), f.getOrden(), f.isEsPortada());
    }
}

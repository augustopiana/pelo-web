package com.peloweb.vinilos.catalog;

import com.peloweb.vinilos.domain.Vinilo;
import com.peloweb.vinilos.domain.enums.EstadoDisco;
import com.peloweb.vinilos.domain.enums.EstadoVinilo;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Specifications para las queries del catalogo publico. */
public final class ViniloSpecs {

    private ViniloSpecs() {
    }

    /**
     * Visibilidad publica (R-9): oculta PAUSADO y los VENDIDO con mas de 30 dias
     * desde fecha_venta. Se conservan en la base.
     */
    public static Specification<Vinilo> visiblePublico() {
        return (root, query, cb) -> {
            OffsetDateTime corte = OffsetDateTime.now().minusDays(30);
            var noPausado = cb.notEqual(root.get("estado"), EstadoVinilo.PAUSADO);
            var vendidoViejo = cb.and(
                    cb.equal(root.get("estado"), EstadoVinilo.VENDIDO),
                    cb.lessThan(root.get("fechaVenta"), corte));
            return cb.and(noPausado, cb.not(vendidoViejo));
        };
    }

    /** Busqueda por texto en titulo o artista. */
    public static Specification<Vinilo> texto(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("titulo")), like),
                cb.like(cb.lower(root.get("artista")), like));
    }

    public static Specification<Vinilo> artista(String artista) {
        String like = "%" + artista.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("artista")), like);
    }

    public static Specification<Vinilo> genero(UUID generoId) {
        return (root, query, cb) -> cb.equal(root.get("genero").get("id"), generoId);
    }

    public static Specification<Vinilo> precioMin(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("precio"), min);
    }

    public static Specification<Vinilo> precioMax(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("precio"), max);
    }

    public static Specification<Vinilo> estadoDisco(EstadoDisco estadoDisco) {
        return (root, query, cb) -> cb.equal(root.get("estadoDisco"), estadoDisco);
    }
}

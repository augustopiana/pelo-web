package com.peloweb.vinilos.catalog;

import com.peloweb.vinilos.catalog.dto.FotoDTO;
import com.peloweb.vinilos.catalog.dto.GeneroDTO;
import com.peloweb.vinilos.catalog.dto.PageResponse;
import com.peloweb.vinilos.catalog.dto.ViniloDetalleDTO;
import com.peloweb.vinilos.catalog.dto.ViniloResumenDTO;
import com.peloweb.vinilos.domain.FotoVinilo;
import com.peloweb.vinilos.domain.Genero;
import com.peloweb.vinilos.domain.Vinilo;
import com.peloweb.vinilos.domain.enums.EstadoDisco;
import com.peloweb.vinilos.web.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private static final int MAX_SIZE = 60;

    private final ViniloRepository vinilos;
    private final FotoViniloRepository fotos;
    private final GeneroRepository generos;

    public CatalogService(ViniloRepository vinilos, FotoViniloRepository fotos, GeneroRepository generos) {
        this.vinilos = vinilos;
        this.fotos = fotos;
        this.generos = generos;
    }

    public PageResponse<ViniloResumenDTO> buscar(String q, String artista, UUID generoId,
                                                 BigDecimal precioMin, BigDecimal precioMax,
                                                 String estadoDisco, int page, int size) {
        Specification<Vinilo> spec = ViniloSpecs.visiblePublico();
        if (StringUtils.hasText(q)) {
            spec = spec.and(ViniloSpecs.texto(q.trim()));
        }
        if (StringUtils.hasText(artista)) {
            spec = spec.and(ViniloSpecs.artista(artista.trim()));
        }
        if (generoId != null) {
            spec = spec.and(ViniloSpecs.genero(generoId));
        }
        if (precioMin != null) {
            spec = spec.and(ViniloSpecs.precioMin(precioMin));
        }
        if (precioMax != null) {
            spec = spec.and(ViniloSpecs.precioMax(precioMax));
        }
        if (StringUtils.hasText(estadoDisco)) {
            spec = spec.and(ViniloSpecs.estadoDisco(parseEstadoDisco(estadoDisco)));
        }

        int pageSize = Math.min(Math.max(size, 1), MAX_SIZE);
        // Orden por defecto: mas nuevos primero (spec §8).
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize,
                Sort.by(Sort.Direction.DESC, "fechaPublicacion"));

        Page<Vinilo> resultado = vinilos.findAll(spec, pageable);
        Map<UUID, String> portadas = portadasDe(resultado.getContent());

        Page<ViniloResumenDTO> dtos = resultado.map(v -> toResumen(v, portadas.get(v.getId())));
        return PageResponse.of(dtos);
    }

    public ViniloDetalleDTO ficha(UUID id) {
        Vinilo v = vinilos.findOne(ViniloSpecs.visiblePublico()
                        .and((root, query, cb) -> cb.equal(root.get("id"), id)))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vinilo no encontrado"));

        List<FotoDTO> galeria = fotos.findByVinilo_IdOrderByOrdenAsc(v.getId()).stream()
                .map(FotoDTO::from)
                .toList();

        return new ViniloDetalleDTO(
                v.getId(), v.getTitulo(), v.getArtista(), GeneroDTO.from(v.getGenero()),
                v.getAnio(), v.getSello(), v.getEdicionPais(), v.getFormato().name(),
                v.getEstadoDisco().name(), v.getDescripcion(), v.getPrecio(),
                v.getDescuentoCortePct(), v.getEstado().name(),
                v.getFechaPublicacion(), galeria);
    }

    public List<GeneroDTO> generos() {
        return generos.findAllByOrderByNombreAsc().stream().map(GeneroDTO::from).toList();
    }

    private Map<UUID, String> portadasDe(List<Vinilo> lista) {
        if (lista.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = lista.stream().map(Vinilo::getId).toList();
        return fotos.findByVinilo_IdInAndEsPortadaTrue(ids).stream()
                .collect(Collectors.toMap(f -> f.getVinilo().getId(), FotoVinilo::getUrl, (a, b) -> a));
    }

    private ViniloResumenDTO toResumen(Vinilo v, String portadaUrl) {
        Genero g = v.getGenero();
        return new ViniloResumenDTO(
                v.getId(), v.getTitulo(), v.getArtista(),
                g != null ? g.getNombre() : null,
                v.getAnio(), v.getPrecio(), v.getEstadoDisco().name(), v.getEstado().name(),
                v.getDescuentoCortePct(), portadaUrl, v.getFechaPublicacion());
    }

    private EstadoDisco parseEstadoDisco(String valor) {
        try {
            return EstadoDisco.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "estadoDisco invalido: " + valor);
        }
    }
}

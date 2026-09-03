package com.peloweb.vinilos.admin;

import com.peloweb.vinilos.admin.dto.ViniloRequest;
import com.peloweb.vinilos.catalog.FotoViniloRepository;
import com.peloweb.vinilos.catalog.GeneroRepository;
import com.peloweb.vinilos.catalog.ViniloRepository;
import com.peloweb.vinilos.catalog.dto.FotoDTO;
import com.peloweb.vinilos.catalog.dto.GeneroDTO;
import com.peloweb.vinilos.catalog.dto.PageResponse;
import com.peloweb.vinilos.catalog.dto.ViniloDetalleDTO;
import com.peloweb.vinilos.catalog.dto.ViniloResumenDTO;
import com.peloweb.vinilos.domain.FotoVinilo;
import com.peloweb.vinilos.domain.Genero;
import com.peloweb.vinilos.domain.Vinilo;
import com.peloweb.vinilos.domain.enums.EstadoVinilo;
import com.peloweb.vinilos.domain.enums.Formato;
import com.peloweb.vinilos.storage.StorageService;
import com.peloweb.vinilos.web.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminViniloService {

    private static final Map<String, String> EXT_POR_TIPO = Map.of(
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private final ViniloRepository vinilos;
    private final GeneroRepository generos;
    private final FotoViniloRepository fotos;
    private final StorageService storage;

    public AdminViniloService(ViniloRepository vinilos, GeneroRepository generos,
                              FotoViniloRepository fotos, StorageService storage) {
        this.vinilos = vinilos;
        this.generos = generos;
        this.fotos = fotos;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public ViniloDetalleDTO detalle(UUID id) {
        return toDetalle(cargar(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ViniloResumenDTO> listar(int page, int size) {
        Page<Vinilo> pagina = vinilos.findAll(PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "fechaPublicacion")));
        return PageResponse.of(pagina.map(this::toResumen));
    }

    @Transactional
    public ViniloDetalleDTO crear(ViniloRequest r) {
        Vinilo v = new Vinilo();
        v.setId(UUID.randomUUID());
        v.setEstado(EstadoVinilo.DISPONIBLE);
        v.setFormato(Formato.VINILO);
        v.setFechaPublicacion(OffsetDateTime.now());
        v.setCreatedAt(OffsetDateTime.now());
        aplicar(v, r);
        vinilos.save(v);
        return toDetalle(v);
    }

    @Transactional
    public ViniloDetalleDTO actualizar(UUID id, ViniloRequest r) {
        Vinilo v = cargar(id);
        aplicar(v, r);
        vinilos.save(v);
        return toDetalle(v);
    }

    /** Pausa (disponible -> pausado) o reactiva (pausado -> disponible). */
    @Transactional
    public ViniloDetalleDTO togglePausa(UUID id) {
        Vinilo v = cargar(id);
        if (v.getEstado() == EstadoVinilo.DISPONIBLE) {
            v.setEstado(EstadoVinilo.PAUSADO);
        } else if (v.getEstado() == EstadoVinilo.PAUSADO) {
            v.setEstado(EstadoVinilo.DISPONIBLE);
        } else {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede pausar/reactivar un vinilo en estado " + v.getEstado());
        }
        v.setUpdatedAt(OffsetDateTime.now());
        vinilos.save(v);
        return toDetalle(v);
    }

    @Transactional
    public List<FotoDTO> subirFotos(UUID id, MultipartFile[] archivos) {
        Vinilo v = cargar(id);
        if (archivos == null || archivos.length == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se recibieron archivos");
        }
        List<FotoVinilo> existentes = fotos.findByVinilo_IdOrderByOrdenAsc(id);
        int orden = existentes.size();
        boolean hayPortada = existentes.stream().anyMatch(FotoVinilo::isEsPortada);

        for (MultipartFile archivo : archivos) {
            String tipo = archivo.getContentType();
            String ext = EXT_POR_TIPO.get(tipo == null ? "" : tipo.toLowerCase());
            if (ext == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Formato no soportado: " + tipo + " (usar JPG, PNG o WEBP)");
            }
            String key = "vinilos/" + id + "/" + UUID.randomUUID() + "." + ext;
            String url;
            try {
                url = storage.upload(archivo.getBytes(), key, tipo);
            } catch (IOException e) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo leer el archivo subido");
            }

            FotoVinilo f = new FotoVinilo();
            f.setId(UUID.randomUUID());
            f.setVinilo(v);
            f.setUrl(url);
            f.setOrden(orden);
            // La primera foto del vinilo es la portada (spec §4.3).
            f.setEsPortada(!hayPortada && orden == existentes.size());
            fotos.save(f);
            hayPortada = true;
            orden++;
        }
        return fotos.findByVinilo_IdOrderByOrdenAsc(id).stream().map(FotoDTO::from).toList();
    }

    @Transactional
    public void borrarFoto(UUID viniloId, UUID fotoId) {
        FotoVinilo f = fotos.findById(fotoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Foto no encontrada"));
        if (!f.getVinilo().getId().equals(viniloId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La foto no pertenece a ese vinilo");
        }
        try {
            storage.delete(storage.keyFromUrl(f.getUrl()));
        } catch (Exception e) {
            // Si falla el borrado en storage, igual sacamos la referencia de la base.
        }
        fotos.delete(f);
    }

    // ---- helpers ----

    private Vinilo cargar(UUID id) {
        return vinilos.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vinilo no encontrado"));
    }

    private void aplicar(Vinilo v, ViniloRequest r) {
        v.setTitulo(r.titulo().trim());
        v.setArtista(r.artista().trim());
        v.setGenero(resolverGenero(r.generoId()));
        v.setAnio(r.anio());
        v.setSello(r.sello());
        v.setEdicionPais(r.edicionPais());
        v.setEstadoDisco(r.estadoDisco());
        v.setDescripcion(r.descripcion());
        v.setPrecio(r.precio());
        v.setDescuentoCortePct(r.descuentoCortePct());
        v.setUpdatedAt(OffsetDateTime.now());
    }

    private Genero resolverGenero(UUID generoId) {
        if (generoId == null) {
            return null;
        }
        return generos.findById(generoId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Género inexistente"));
    }

    private ViniloResumenDTO toResumen(Vinilo v) {
        String portada = fotos.findByVinilo_IdOrderByOrdenAsc(v.getId()).stream()
                .filter(FotoVinilo::isEsPortada).map(FotoVinilo::getUrl).findFirst().orElse(null);
        return new ViniloResumenDTO(v.getId(), v.getTitulo(), v.getArtista(),
                v.getGenero() != null ? v.getGenero().getNombre() : null,
                v.getAnio(), v.getPrecio(), v.getEstadoDisco().name(), v.getEstado().name(),
                v.getDescuentoCortePct(), portada, v.getFechaPublicacion());
    }

    private ViniloDetalleDTO toDetalle(Vinilo v) {
        List<FotoDTO> galeria = fotos.findByVinilo_IdOrderByOrdenAsc(v.getId()).stream()
                .map(FotoDTO::from).toList();
        return new ViniloDetalleDTO(v.getId(), v.getTitulo(), v.getArtista(), GeneroDTO.from(v.getGenero()),
                v.getAnio(), v.getSello(), v.getEdicionPais(), v.getFormato().name(),
                v.getEstadoDisco().name(), v.getDescripcion(), v.getPrecio(), v.getDescuentoCortePct(),
                v.getEstado().name(), v.getFechaPublicacion(), galeria);
    }
}

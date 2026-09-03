package com.peloweb.vinilos.order;

import com.peloweb.vinilos.account.OrdenRepository;
import com.peloweb.vinilos.catalog.dto.PageResponse;
import com.peloweb.vinilos.domain.DatosEnvio;
import com.peloweb.vinilos.domain.ItemOrden;
import com.peloweb.vinilos.domain.Orden;
import com.peloweb.vinilos.domain.Usuario;
import com.peloweb.vinilos.domain.enums.EstadoOrden;
import com.peloweb.vinilos.domain.enums.ModoEntrega;
import com.peloweb.vinilos.order.dto.EnvioDTO;
import com.peloweb.vinilos.order.dto.ItemLineaDTO;
import com.peloweb.vinilos.order.dto.OrdenAdminDTO;
import com.peloweb.vinilos.web.ApiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Gestión de órdenes por el dueño (E6): retiro (entrega) y envío (despacho). */
@Service
public class AdminOrdenService {

    private final OrdenRepository ordenes;
    private final ItemOrdenRepository items;

    public AdminOrdenService(OrdenRepository ordenes, ItemOrdenRepository items) {
        this.ordenes = ordenes;
        this.items = items;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrdenAdminDTO> listar(int page, int size) {
        var pagina = ordenes.findAllByOrderByCreatedAtDesc(
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
        return PageResponse.of(pagina.map(this::toDTO));
    }

    @Transactional(readOnly = true)
    public OrdenAdminDTO buscarPorCodigo(String codigo) {
        Orden o = ordenes.findByCodigoRetiro(codigo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No hay una orden con ese código"));
        return toDTO(o);
    }

    /** Confirma la entrega en el local de una orden de retiro (Flujo B). */
    @Transactional
    public OrdenAdminDTO entregar(String codigo) {
        Orden o = ordenes.findByCodigoRetiro(codigo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No hay una orden con ese código"));
        if (o.getModoEntrega() != ModoEntrega.RETIRO) {
            throw new ApiException(HttpStatus.CONFLICT, "Esa orden es de envío, no de retiro");
        }
        if (o.getEstado() != EstadoOrden.PAGADA) {
            throw new ApiException(HttpStatus.CONFLICT, "La orden no está lista para entregar (estado " + o.getEstado() + ")");
        }
        o.setEstado(EstadoOrden.ENTREGADA);
        o.setFechaEntrega(OffsetDateTime.now());
        ordenes.save(o);
        return toDTO(o);
    }

    /** Marca una orden de envío como despachada por correo (Flujo C). */
    @Transactional
    public OrdenAdminDTO despachar(UUID ordenId) {
        Orden o = ordenes.findById(ordenId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        if (o.getModoEntrega() != ModoEntrega.ENVIO) {
            throw new ApiException(HttpStatus.CONFLICT, "Esa orden es de retiro, no de envío");
        }
        if (o.getEstado() != EstadoOrden.PAGADA) {
            throw new ApiException(HttpStatus.CONFLICT, "La orden no está lista para despachar (estado " + o.getEstado() + ")");
        }
        o.setEstado(EstadoOrden.ENVIADA);
        o.setFechaDespacho(OffsetDateTime.now());
        ordenes.save(o);
        return toDTO(o);
    }

    private OrdenAdminDTO toDTO(Orden o) {
        Usuario u = o.getUsuario();
        List<ItemLineaDTO> lineas = items.findByOrden_Id(o.getId()).stream()
                .map(this::toLinea)
                .toList();
        return new OrdenAdminDTO(
                o.getId(), o.getEstado().name(), o.getModoEntrega().name(), o.getTotal(),
                o.getCodigoRetiro(), o.getCreatedAt(), o.getFechaPago(), o.getFechaEntrega(),
                o.getFechaDespacho(), u.getNombre(), u.getEmail(), u.getTelefono(),
                toEnvio(o.getEnvio()), lineas);
    }

    private ItemLineaDTO toLinea(ItemOrden it) {
        return new ItemLineaDTO(it.getVinilo().getId(), it.getVinilo().getTitulo(),
                it.getVinilo().getArtista(), it.getPrecio());
    }

    private EnvioDTO toEnvio(DatosEnvio e) {
        if (e == null || e.getDireccion() == null) {
            return null;
        }
        return new EnvioDTO(e.getNombre(), e.getTelefono(), e.getDireccion(),
                e.getLocalidad(), e.getProvincia(), e.getCp());
    }
}

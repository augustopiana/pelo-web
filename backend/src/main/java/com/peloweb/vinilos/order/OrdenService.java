package com.peloweb.vinilos.order;

import com.peloweb.vinilos.account.OrdenRepository;
import com.peloweb.vinilos.catalog.ViniloRepository;
import com.peloweb.vinilos.domain.DatosEnvio;
import com.peloweb.vinilos.domain.ItemOrden;
import com.peloweb.vinilos.domain.Orden;
import com.peloweb.vinilos.domain.Pago;
import com.peloweb.vinilos.domain.Usuario;
import com.peloweb.vinilos.domain.Vinilo;
import com.peloweb.vinilos.domain.enums.EstadoOrden;
import com.peloweb.vinilos.domain.enums.EstadoPago;
import com.peloweb.vinilos.domain.enums.EstadoVinilo;
import com.peloweb.vinilos.domain.enums.MedioPago;
import com.peloweb.vinilos.domain.enums.ModoEntrega;
import com.peloweb.vinilos.order.dto.CheckoutResponse;
import com.peloweb.vinilos.order.dto.CrearOrdenRequest;
import com.peloweb.vinilos.order.dto.EnvioDTO;
import com.peloweb.vinilos.payment.PaymentGateway;
import com.peloweb.vinilos.security.AuthUser;
import com.peloweb.vinilos.user.UsuarioRepository;
import com.peloweb.vinilos.web.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * Compra directa (E5). Crea la orden bloqueando las piezas (R-11) e inicia el checkout;
 * la confirmación llega por webhook (idempotente) y es la única fuente de verdad del pago.
 */
@Service
public class OrdenService {

    private static final Logger log = LoggerFactory.getLogger(OrdenService.class);

    private final OrdenRepository ordenes;
    private final ItemOrdenRepository items;
    private final PagoRepository pagos;
    private final ViniloRepository vinilos;
    private final UsuarioRepository usuarios;
    private final PaymentGateway gateway;
    private final CodigoRetiroGenerator codigoGen;
    private final int holdMin;

    public OrdenService(OrdenRepository ordenes, ItemOrdenRepository items, PagoRepository pagos,
                        ViniloRepository vinilos, UsuarioRepository usuarios, PaymentGateway gateway,
                        CodigoRetiroGenerator codigoGen, @Value("${app.payments.hold-min}") int holdMin) {
        this.ordenes = ordenes;
        this.items = items;
        this.pagos = pagos;
        this.vinilos = vinilos;
        this.usuarios = usuarios;
        this.gateway = gateway;
        this.codigoGen = codigoGen;
        this.holdMin = holdMin;
    }

    @Transactional
    public CheckoutResponse crear(AuthUser principal, CrearOrdenRequest req) {
        Usuario u = usuarios.findById(principal.id())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        if (!u.isEmailVerificado()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Verificá tu email para poder comprar");
        }
        if (req.modoEntrega() == ModoEntrega.ENVIO && req.envio() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Faltan los datos de envío");
        }

        OffsetDateTime ahora = OffsetDateTime.now();
        OffsetDateTime holdHasta = ahora.plusMinutes(holdMin);

        // Bloquear y validar cada vinilo (pieza única, R-11).
        List<UUID> ids = new LinkedHashSet<>(req.viniloIds()).stream().toList();
        BigDecimal total = BigDecimal.ZERO;
        Orden orden = new Orden();
        orden.setId(UUID.randomUUID());
        orden.setUsuario(u);
        orden.setEstado(EstadoOrden.PENDIENTE_PAGO);
        orden.setModoEntrega(req.modoEntrega());
        orden.setEnvio(mapEnvio(req.envio()));
        orden.setCreatedAt(ahora);

        java.util.List<Vinilo> comprados = new java.util.ArrayList<>();
        for (UUID id : ids) {
            Vinilo v = vinilos.findByIdForUpdate(id)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vinilo no encontrado"));
            if (v.getEstado() != EstadoVinilo.DISPONIBLE) {
                throw new ApiException(HttpStatus.CONFLICT, "Ya no está disponible: " + v.getTitulo());
            }
            if (v.getBloqueoHasta() != null && v.getBloqueoHasta().isAfter(ahora)) {
                throw new ApiException(HttpStatus.CONFLICT, "Otro cliente lo está comprando: " + v.getTitulo());
            }
            v.setBloqueoHasta(holdHasta);
            vinilos.save(v);
            comprados.add(v);
            total = total.add(v.getPrecio());
        }

        orden.setTotal(total);
        ordenes.save(orden);

        for (Vinilo v : comprados) {
            ItemOrden it = new ItemOrden();
            it.setId(UUID.randomUUID());
            it.setOrden(orden);
            it.setVinilo(v);
            it.setPrecio(v.getPrecio());
            it.setDescuentoCortePct(v.getDescuentoCortePct());
            items.save(it);
        }

        Pago pago = new Pago();
        pago.setId(UUID.randomUUID());
        pago.setOrden(orden);
        pago.setMonto(total);
        pago.setMedio(MedioPago.MERCADOPAGO);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setCreatedAt(ahora);

        PaymentGateway.CheckoutInit init = gateway.iniciarCheckout(orden);
        pago.setMpPaymentId(init.referenciaPago());
        pagos.save(pago);

        return new CheckoutResponse(orden.getId(), init.checkoutUrl());
    }

    /**
     * Confirma (o rechaza) el pago de una orden. Idempotente: si el pago ya fue procesado,
     * no hace nada. Es la ÚNICA fuente de verdad del pago (llamado desde el webhook / dev).
     */
    @Transactional
    public void confirmarPago(String referenciaPago, boolean aprobado) {
        Pago pago = pagos.findByMpPaymentId(referenciaPago).orElse(null);
        if (pago == null) {
            log.warn("[pago] referencia desconocida: {}", referenciaPago);
            return;
        }
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            // Ya procesado: idempotencia.
            return;
        }
        Orden orden = pago.getOrden();
        OffsetDateTime ahora = OffsetDateTime.now();

        if (aprobado) {
            pago.setEstado(EstadoPago.APROBADO);
            pagos.save(pago);

            orden.setEstado(EstadoOrden.PAGADA);
            orden.setFechaPago(ahora);
            orden.setMontoPagado(orden.getTotal());
            if (orden.getModoEntrega() == ModoEntrega.RETIRO) {
                orden.setCodigoRetiro(codigoGen.generar());
            }
            ordenes.save(orden);

            for (ItemOrden it : items.findByOrden_Id(orden.getId())) {
                Vinilo v = vinilos.findByIdForUpdate(it.getVinilo().getId()).orElse(null);
                if (v != null && v.getEstado() == EstadoVinilo.DISPONIBLE) {
                    v.setEstado(EstadoVinilo.VENDIDO);
                    v.setFechaVenta(ahora);
                    v.setBloqueoHasta(null);
                    vinilos.save(v);
                }
            }
            // Cupón y notificaciones: Milestone 6.
        } else {
            pago.setEstado(EstadoPago.RECHAZADO);
            pagos.save(pago);

            orden.setEstado(EstadoOrden.CANCELADA);
            ordenes.save(orden);

            for (ItemOrden it : items.findByOrden_Id(orden.getId())) {
                Vinilo v = vinilos.findByIdForUpdate(it.getVinilo().getId()).orElse(null);
                if (v != null && v.getEstado() == EstadoVinilo.DISPONIBLE) {
                    v.setBloqueoHasta(null);
                    vinilos.save(v);
                }
            }
        }
    }

    /** Referencia de pago (para simular/confirmar) a partir del id de orden. */
    @Transactional(readOnly = true)
    public String referenciaPagoDeOrden(UUID ordenId) {
        return pagos.findByOrden_Id(ordenId)
                .map(Pago::getMpPaymentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden sin pago"));
    }

    private DatosEnvio mapEnvio(EnvioDTO dto) {
        if (dto == null) {
            return null;
        }
        DatosEnvio e = new DatosEnvio();
        e.setNombre(dto.nombre());
        e.setTelefono(dto.telefono());
        e.setDireccion(dto.direccion());
        e.setLocalidad(dto.localidad());
        e.setProvincia(dto.provincia());
        e.setCp(dto.cp());
        return e;
    }
}

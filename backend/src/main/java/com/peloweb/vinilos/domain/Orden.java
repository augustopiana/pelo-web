package com.peloweb.vinilos.domain;

import com.peloweb.vinilos.domain.enums.EstadoOrden;
import com.peloweb.vinilos.domain.enums.ModoEntrega;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Orden de compra directa (spec 4.5, 5.2, v0.2). Pago 100% online; entrega por
 * retiro (con código) o envío por correo.
 */
@Entity
@Table(name = "orden")
public class Orden {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "monto_pagado", precision = 12, scale = 2)
    private BigDecimal montoPagado;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_entrega", nullable = false)
    private ModoEntrega modoEntrega;

    /** Aleatorio, único y legible; se genera al confirmarse el pago si es retiro (R-13). */
    @Column(name = "codigo_retiro", unique = true)
    private String codigoRetiro;

    @Column(name = "fecha_pago")
    private OffsetDateTime fechaPago;

    /** Retiro: cuando el dueño confirma la entrega. */
    @Column(name = "fecha_entrega")
    private OffsetDateTime fechaEntrega;

    /** Envío: cuando el dueño marca que despachó por correo. */
    @Column(name = "fecha_despacho")
    private OffsetDateTime fechaDespacho;

    @Embedded
    private DatosEnvio envio;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public ModoEntrega getModoEntrega() {
        return modoEntrega;
    }

    public void setModoEntrega(ModoEntrega modoEntrega) {
        this.modoEntrega = modoEntrega;
    }

    public String getCodigoRetiro() {
        return codigoRetiro;
    }

    public void setCodigoRetiro(String codigoRetiro) {
        this.codigoRetiro = codigoRetiro;
    }

    public OffsetDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(OffsetDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public OffsetDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(OffsetDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public OffsetDateTime getFechaDespacho() {
        return fechaDespacho;
    }

    public void setFechaDespacho(OffsetDateTime fechaDespacho) {
        this.fechaDespacho = fechaDespacho;
    }

    public DatosEnvio getEnvio() {
        return envio;
    }

    public void setEnvio(DatosEnvio envio) {
        this.envio = envio;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

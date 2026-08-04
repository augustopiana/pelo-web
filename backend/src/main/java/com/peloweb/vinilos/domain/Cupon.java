package com.peloweb.vinilos.domain;

import com.peloweb.vinilos.domain.enums.EstadoCupon;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Cupon de descuento en corte generado por una compra (spec 4.9, R-8). */
@Entity
@Table(name = "cupon")
public class Cupon {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    /** El mayor % entre los items vendidos de la orden. */
    @Column(nullable = false)
    private Integer porcentaje;

    @Column(name = "fecha_generacion", nullable = false)
    private OffsetDateTime fechaGeneracion;

    /** Generacion + 2 meses. */
    @Column(name = "fecha_vencimiento", nullable = false)
    private OffsetDateTime fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCupon estado = EstadoCupon.ACTIVO;

    /** Se completa al redimir (modulo turnos, futuro). */
    @Column(name = "fecha_uso")
    private OffsetDateTime fechaUso;

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

    public Orden getOrden() {
        return orden;
    }

    public void setOrden(Orden orden) {
        this.orden = orden;
    }

    public Integer getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Integer porcentaje) {
        this.porcentaje = porcentaje;
    }

    public OffsetDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(OffsetDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public OffsetDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(OffsetDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public EstadoCupon getEstado() {
        return estado;
    }

    public void setEstado(EstadoCupon estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFechaUso() {
        return fechaUso;
    }

    public void setFechaUso(OffsetDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }
}

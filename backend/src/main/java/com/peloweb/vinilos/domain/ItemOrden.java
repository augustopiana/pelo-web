package com.peloweb.vinilos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/** Item de una orden: un vinilo comprado (spec 4.6, v0.2). */
@Entity
@Table(name = "item_orden")
public class ItemOrden {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vinilo_id", nullable = false)
    private Vinilo vinilo;

    /** Copia del precio al momento de la orden. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    /** Copia del % de descuento en corte al momento de la orden. */
    @Column(name = "descuento_corte_pct", nullable = false)
    private Integer descuentoCortePct;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Orden getOrden() {
        return orden;
    }

    public void setOrden(Orden orden) {
        this.orden = orden;
    }

    public Vinilo getVinilo() {
        return vinilo;
    }

    public void setVinilo(Vinilo vinilo) {
        this.vinilo = vinilo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getDescuentoCortePct() {
        return descuentoCortePct;
    }

    public void setDescuentoCortePct(Integer descuentoCortePct) {
        this.descuentoCortePct = descuentoCortePct;
    }
}

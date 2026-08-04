package com.peloweb.vinilos.domain;

import com.peloweb.vinilos.domain.enums.EstadoItem;
import com.peloweb.vinilos.domain.enums.MetodoResto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/** Item de una orden: un vinilo dentro de la orden (spec 4.6). */
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

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_item", nullable = false)
    private EstadoItem estadoItem = EstadoItem.PENDIENTE;

    /** Solo sena. */
    @Column(name = "resto_pagado", nullable = false)
    private boolean restoPagado = false;

    /** Nulo hasta que se paga el resto. */
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_resto")
    private MetodoResto metodoResto;

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

    public EstadoItem getEstadoItem() {
        return estadoItem;
    }

    public void setEstadoItem(EstadoItem estadoItem) {
        this.estadoItem = estadoItem;
    }

    public boolean isRestoPagado() {
        return restoPagado;
    }

    public void setRestoPagado(boolean restoPagado) {
        this.restoPagado = restoPagado;
    }

    public MetodoResto getMetodoResto() {
        return metodoResto;
    }

    public void setMetodoResto(MetodoResto metodoResto) {
        this.metodoResto = metodoResto;
    }
}

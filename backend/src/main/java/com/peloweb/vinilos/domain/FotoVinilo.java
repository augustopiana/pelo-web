package com.peloweb.vinilos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/** Foto de la galeria de un vinilo (spec 4.3). La primera (es_portada) es la tapa. */
@Entity
@Table(name = "foto_vinilo")
public class FotoVinilo {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vinilo_id", nullable = false)
    private Vinilo vinilo;

    @Column(nullable = false)
    private String url;

    /** Posicion en la galeria. */
    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "es_portada", nullable = false)
    private boolean esPortada = false;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Vinilo getVinilo() {
        return vinilo;
    }

    public void setVinilo(Vinilo vinilo) {
        this.vinilo = vinilo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public boolean isEsPortada() {
        return esPortada;
    }

    public void setEsPortada(boolean esPortada) {
        this.esPortada = esPortada;
    }
}

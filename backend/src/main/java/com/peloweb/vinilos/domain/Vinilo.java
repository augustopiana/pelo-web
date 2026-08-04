package com.peloweb.vinilos.domain;

import com.peloweb.vinilos.domain.enums.EstadoDisco;
import com.peloweb.vinilos.domain.enums.EstadoVinilo;
import com.peloweb.vinilos.domain.enums.Formato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Vinilo: pieza unica (spec 4.2). Su ciclo de vida esta en spec 5.1. */
@Entity
@Table(name = "vinilo")
public class Vinilo {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String artista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genero_id")
    private Genero genero;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "sello")
    private String sello;

    @Column(name = "edicion_pais")
    private String edicionPais;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Formato formato = Formato.VINILO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_disco", nullable = false)
    private EstadoDisco estadoDisco;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    /** % de descuento en corte que otorga este vinilo (ej. 15). */
    @Column(name = "descuento_corte_pct", nullable = false)
    private Integer descuentoCortePct = 0;

    /** true: solo se puede senar. false: solo compra directa (spec 4.2, R-10). */
    @Column(nullable = false)
    private boolean senable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVinilo estado = EstadoVinilo.DISPONIBLE;

    @Column(name = "fecha_publicacion", nullable = false)
    private OffsetDateTime fechaPublicacion;

    /** Se setea al marcar vendido; controla el ocultamiento a 30 dias (R-9). */
    @Column(name = "fecha_venta")
    private OffsetDateTime fechaVenta;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** Bloqueo optimista para evitar doble reserva de la pieza unica (R-11). */
    @Version
    @Column(nullable = false)
    private Long version;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getSello() {
        return sello;
    }

    public void setSello(String sello) {
        this.sello = sello;
    }

    public String getEdicionPais() {
        return edicionPais;
    }

    public void setEdicionPais(String edicionPais) {
        this.edicionPais = edicionPais;
    }

    public Formato getFormato() {
        return formato;
    }

    public void setFormato(Formato formato) {
        this.formato = formato;
    }

    public EstadoDisco getEstadoDisco() {
        return estadoDisco;
    }

    public void setEstadoDisco(EstadoDisco estadoDisco) {
        this.estadoDisco = estadoDisco;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public boolean isSenable() {
        return senable;
    }

    public void setSenable(boolean senable) {
        this.senable = senable;
    }

    public EstadoVinilo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVinilo estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(OffsetDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public OffsetDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(OffsetDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}

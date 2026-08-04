package com.peloweb.vinilos.domain;

import com.peloweb.vinilos.domain.enums.MotivoReembolso;
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
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Devolucion de sena (spec 4.8). En ordenes multi-vinilo la devolucion es por item.
 * Motivo R-3 (rechazo al probar) o R-4 (cancelacion voluntaria).
 */
@Entity
@Table(name = "reembolso")
public class Reembolso {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemOrden item;

    /** 100% de la sena de ese item. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /** Id de la devolucion en MP. */
    @Column(name = "mp_refund_id")
    private String mpRefundId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoReembolso motivo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public ItemOrden getItem() {
        return item;
    }

    public void setItem(ItemOrden item) {
        this.item = item;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMpRefundId() {
        return mpRefundId;
    }

    public void setMpRefundId(String mpRefundId) {
        this.mpRefundId = mpRefundId;
    }

    public MotivoReembolso getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoReembolso motivo) {
        this.motivo = motivo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

package com.peloweb.vinilos.order;

import com.peloweb.vinilos.domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PagoRepository extends JpaRepository<Pago, UUID> {

    Optional<Pago> findByMpPaymentId(String mpPaymentId);

    Optional<Pago> findByOrden_Id(UUID ordenId);
}

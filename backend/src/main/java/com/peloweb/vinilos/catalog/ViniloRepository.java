package com.peloweb.vinilos.catalog;

import com.peloweb.vinilos.domain.Vinilo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ViniloRepository extends JpaRepository<Vinilo, UUID>, JpaSpecificationExecutor<Vinilo> {

    /** Bloqueo pesimista de la fila para evitar doble venta (R-11). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vinilo v where v.id = :id")
    Optional<Vinilo> findByIdForUpdate(UUID id);
}

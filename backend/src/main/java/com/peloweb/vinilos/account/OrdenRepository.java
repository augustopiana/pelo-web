package com.peloweb.vinilos.account;

import com.peloweb.vinilos.domain.Orden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdenRepository extends JpaRepository<Orden, UUID> {

    List<Orden> findByUsuario_IdOrderByCreatedAtDesc(UUID usuarioId);

    boolean existsByCodigoRetiro(String codigoRetiro);

    Optional<Orden> findByCodigoRetiro(String codigoRetiro);

    Page<Orden> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

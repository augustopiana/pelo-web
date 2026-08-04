package com.peloweb.vinilos.account;

import com.peloweb.vinilos.domain.Cupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CuponRepository extends JpaRepository<Cupon, UUID> {

    List<Cupon> findByUsuario_IdOrderByFechaGeneracionDesc(UUID usuarioId);
}

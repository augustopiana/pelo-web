package com.peloweb.vinilos.catalog;

import com.peloweb.vinilos.domain.Genero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GeneroRepository extends JpaRepository<Genero, UUID> {

    List<Genero> findAllByOrderByNombreAsc();
}

package com.peloweb.vinilos.catalog;

import com.peloweb.vinilos.domain.Vinilo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ViniloRepository extends JpaRepository<Vinilo, UUID>, JpaSpecificationExecutor<Vinilo> {
}

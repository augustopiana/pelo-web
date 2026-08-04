package com.peloweb.vinilos.catalog;

import com.peloweb.vinilos.domain.FotoVinilo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FotoViniloRepository extends JpaRepository<FotoVinilo, UUID> {

    List<FotoVinilo> findByVinilo_IdOrderByOrdenAsc(UUID viniloId);

    List<FotoVinilo> findByVinilo_IdInAndEsPortadaTrue(Collection<UUID> viniloIds);
}

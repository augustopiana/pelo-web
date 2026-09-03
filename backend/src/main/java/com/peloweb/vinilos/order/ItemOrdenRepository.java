package com.peloweb.vinilos.order;

import com.peloweb.vinilos.domain.ItemOrden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemOrdenRepository extends JpaRepository<ItemOrden, UUID> {

    List<ItemOrden> findByOrden_Id(UUID ordenId);
}

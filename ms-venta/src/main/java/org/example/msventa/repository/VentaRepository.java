package org.example.msventa.repository;

import org.example.msventa.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    List<Venta> findByClienteId(Integer clienteId);
}

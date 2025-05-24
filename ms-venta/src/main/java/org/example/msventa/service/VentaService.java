package org.example.msventa.service;

import org.example.msventa.entity.Venta;
import java.util.List;
import java.util.Optional;

public interface VentaService {
    List<Venta> listar();
    Optional<Venta> obtener(Integer id);
    Venta registrar(Venta venta);
    void eliminar(Integer id);
}
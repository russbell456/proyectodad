package org.example.msventa.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.msventa.entity.Venta;
import org.example.msventa.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public ResponseEntity<List<Venta>> listar() {
        return ResponseEntity.ok(ventaService.listar());
    }

    @GetMapping("/{id}")
    @CircuitBreaker(name = "ventaListarPorIdCB", fallbackMethod = "fallbackVentaPorId")
    public ResponseEntity<Venta> obtener(@PathVariable Integer id) {
        return ventaService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Venta> fallbackVentaPorId(Integer id, Throwable throwable) {
        Venta fallback = new Venta();
        fallback.setId(999);
        fallback.setObservacion("No se pudo obtener la venta. Servicio no disponible.");
        return ResponseEntity.ok(fallback);
    }

    @PostMapping
    public ResponseEntity<Venta> registrar(@RequestBody Venta venta) {
        return ResponseEntity.ok(ventaService.registrar(venta));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        ventaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
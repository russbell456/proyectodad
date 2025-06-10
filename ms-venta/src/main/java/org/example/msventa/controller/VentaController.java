package org.example.msventa.controller;

import org.example.msventa.entity.Venta;
import org.example.msventa.service.VentaService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventas")
public class VentaController {

    @Autowired private VentaService ventaService;

    @GetMapping
    public ResponseEntity<List<Venta>> listar() {
        return ResponseEntity.ok(ventaService.listar());
    }

    @GetMapping("/{id}")
    @CircuitBreaker(name = "ventaCB", fallbackMethod = "fallbackVenta")
    public ResponseEntity<Venta> obtener(@PathVariable Integer id) {
        return ventaService.obtener(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Venta> fallbackVenta(Integer id, Throwable throwable) {
        Venta fallback = new Venta();
        fallback.setId(0);
        fallback.setObservacion("Venta no disponible temporalmente.");
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
    @GetMapping("/pendientes/{clienteId}")
    public ResponseEntity<List<Venta>> pendientes(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(ventaService.pendientes(clienteId));
    }

    /** 🔹 Marca la venta como PAGADA (lo invoca ms‑pago al finalizar un cobro) */
    @PutMapping("/{id}/marcar-pagada")
    public ResponseEntity<Void> marcarPagada(@PathVariable Integer id) {
        ventaService.marcarPagada(id);
        return ResponseEntity.ok().build();
    }
}
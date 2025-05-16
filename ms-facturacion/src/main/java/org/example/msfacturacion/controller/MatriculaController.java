package org.example.msfacturacion.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.msfacturacion.entity.Matricula;
import org.example.msfacturacion.service.MatriculaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    @Autowired
    private MatriculaService matriculaService;

    @GetMapping
    public ResponseEntity<List<Matricula>> listar() {
        return ResponseEntity.ok(matriculaService.listar());
    }

    @GetMapping("/{id}")
    @CircuitBreaker(name = "matriculaListarPorIdCB", fallbackMethod = "fallbackMatriculaPorId")
    public ResponseEntity<Matricula> obtener(@PathVariable Integer id) {
        return matriculaService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Matricula> fallbackMatriculaPorId(Integer id, Throwable throwable) {
        Matricula fallback = new Matricula();
        fallback.setId(999);
        fallback.setObservacion("No se pudo obtener la matrícula. Servicio no disponible.");
        return ResponseEntity.ok(fallback);
    }

    @PostMapping
    public ResponseEntity<Matricula> registrar(@RequestBody Matricula matricula) {
        return ResponseEntity.ok(matriculaService.registrar(matricula));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        matriculaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

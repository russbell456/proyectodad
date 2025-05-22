package org.example.msventa.feign;

import org.example.msventa.dato.Estudiante;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "rdcm-estudiante-service", path = "/estudiantes")
public interface EstudianteFeign {
    @GetMapping("/{id}")
    ResponseEntity<Estudiante> obtenerPorId(@PathVariable Integer id);
}

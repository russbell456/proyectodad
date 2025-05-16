package org.example.rdcmmatricula.feign;

import org.example.rdcmmatricula.dato.Curso;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "rdcm-curso-service", path = "/cursos")
public interface CursoFeign {

    @GetMapping("/{id}")
    ResponseEntity<Curso> obtenerPorId(@PathVariable Integer id);

    @PutMapping ("/{id}")
    ResponseEntity<Curso> actualizarCurso(@PathVariable Integer id, @RequestBody Curso curso);
}

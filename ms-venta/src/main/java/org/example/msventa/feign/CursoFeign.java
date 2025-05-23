package org.example.msventa.feign;

import org.example.msventa.dato.Cliente;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "rdcm-curso-service", path = "/cursos")
public interface CursoFeign {

    @GetMapping("/{id}")
    ResponseEntity<Cliente> obtenerPorId(@PathVariable Integer id);

    @PutMapping ("/{id}")
    ResponseEntity<Cliente> actualizarCurso(@PathVariable Integer id, @RequestBody Cliente cliente);
}

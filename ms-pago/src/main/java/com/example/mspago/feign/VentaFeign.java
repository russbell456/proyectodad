package com.example.mspago.feign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "ms-venta-service", path = "/ventas")
public interface VentaFeign {

    @GetMapping("/pendientes/{clienteId}")
    ResponseEntity<List<VentaDTO>> listarPendientes(@PathVariable Long clienteId);

    @GetMapping("/{id}")
    ResponseEntity<VentaDTO> obtener(@PathVariable Long id);

    @PutMapping("/{id}/marcar-pagada")
    ResponseEntity<Void> marcarPagada(@PathVariable Long id);
}


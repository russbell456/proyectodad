package org.example.msfacturacion.controller;

import org.example.msfacturacion.dato.FacturaDTO;
import org.example.msfacturacion.dato.FacturaRequest;
import org.example.msfacturacion.dato.VentaDTO;
import org.example.msfacturacion.service.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/facturas")
public class FacturaController {

    @Autowired private FacturaService service;

    /* Lista ventas PAGADAS de un cliente para elegir */
    @GetMapping("/ventas-pagadas/{clienteId}")
    public List<VentaDTO> pagadas(@PathVariable Long clienteId){
        return service.ventasPagadas(clienteId);
    }

    /* Emitir factura con varias ventas */
    @PostMapping
    public ResponseEntity<FacturaDTO> emitir(@RequestBody FacturaRequest req){
        return ResponseEntity.ok(service.emitir(req));
    }

    /* Filtrar facturas por cliente */
    @GetMapping("/cliente/{clienteId}")
    public List<FacturaDTO> porCliente(@PathVariable Long clienteId){
        return service.listarPorCliente(clienteId);
    }
}
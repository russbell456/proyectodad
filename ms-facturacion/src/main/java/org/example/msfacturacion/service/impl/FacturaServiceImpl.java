package org.example.msfacturacion.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.msfacturacion.config.EmisorProperties;
import org.example.msfacturacion.dato.*;
import org.example.msfacturacion.entity.Factura;
import org.example.msfacturacion.entity.FacturaDetalle;
import org.example.msfacturacion.feign.ClienteFeign;
import org.example.msfacturacion.feign.VentaFeign;
import org.example.msfacturacion.repository.FacturaRepository;
import org.example.msfacturacion.service.FacturaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {

    private final VentaFeign ventaFeign;
    private final ClienteFeign clienteFeign;
    private final FacturaRepository facturaRepo;
    private final EmisorProperties emisor;

    /* ===== Método 1: listar ventas pagadas de un cliente ===== */
    @Override
    public List<VentaDTO> ventasPagadas(Long clienteId) {
        return ventaFeign.pagadas(clienteId).getBody();
    }

    /* ===== Método 2: emitir una factura con varias ventas ===== */
    @Override
    @Transactional
    public FacturaDTO emitir(FacturaRequest req) {

        /* 1. Traer ventas y filtrar solo PAGADAS */
        List<VentaDTO> ventas = req.getVentasIds().stream()
                .map(id -> ventaFeign.obtener(id).getBody())
                .filter(v -> "PAGADA".equals(v.getEstado()))
                .toList();

        if (ventas.isEmpty()) throw new RuntimeException("No hay ventas válidas");

        /* 2. Validar que todas pertenecen al mismo cliente */
        Long clienteId = ventas.get(0).getClienteId();
        boolean mismoCliente = ventas.stream()
                .allMatch(v -> v.getClienteId().equals(clienteId));
        if (!mismoCliente)
            throw new RuntimeException("Todas las ventas deben ser del mismo cliente");

        /* 3. Datos del cliente */
        DatosClienteDTO cliente = clienteFeign.obtener(clienteId).getBody();

        /* 4. Construir entidad Factura */
        Factura factura = new Factura();
        factura.setNumeroFactura(generarSerieCorrelativo());
        factura.setFechaEmision(LocalDate.now());
        factura.setMoneda(req.getMoneda());
        factura.setTipoComprobante("FACTURA");
        factura.setFormaPago(req.getFormaPago());
        factura.setMedioPago(req.getMedioPago());
        factura.setClienteId(clienteId);
        factura.setRucDniCliente(cliente.getRucDni());
        factura.setNombreCliente(cliente.getNombre());

        /* Detalles y totales */
        double subTotal = 0;
        List<FacturaDetalle> detalles = new ArrayList<>();

        for (VentaDTO v : ventas) {
            for (DetalleVentaDTO dv : v.getDetalles()) {
                FacturaDetalle fd = new FacturaDetalle();
                fd.setVentaId(v.getId());
                fd.setProductoId(dv.getProductoId());
                fd.setDescripcion(dv.getDescripcion());
                fd.setCantidad(dv.getCantidad());
                fd.setUnidadMedida("UN");
                fd.setPrecioUnitario(dv.getPrecioUnitario());
                fd.setSubtotal(dv.getSubtotal());
                fd.setIgv(dv.getSubtotal() * 0.18);
                fd.setTotalLinea(dv.getSubtotal() * 1.18);
                subTotal += dv.getSubtotal();
                detalles.add(fd);
            }
        }

        factura.setSubTotal(subTotal);
        factura.setTotalImpuestos(subTotal * 0.18);
        factura.setTotal(subTotal * 1.18);
        factura.setDetalles(detalles);

        /* 5. Guardar factura */
        facturaRepo.save(factura);

        /* 6. Marcar ventas como FACTURADAS */
        ventas.forEach(v -> ventaFeign.facturar(v.getId()));

        /* 7. Regresar DTO */
        return mapperToDTO(factura);
    }

    /* ===== Método 3: listar facturas por cliente ===== */
    @Override
    public List<FacturaDTO> listarPorCliente(Long clienteId) {
        return facturaRepo.findByClienteId(clienteId)
                .stream()
                .map(this::mapperToDTO)
                .toList();
    }

    /* ---------- Helpers ---------- */

    private static final AtomicLong SEQ = new AtomicLong(1);

    /** Genera serie y correlativo simple: F001-00000001, F001-00000002 ... */
    private String generarSerieCorrelativo() {
        return "F001-" + String.format("%08d", SEQ.getAndIncrement());
    }

    /** Convierte entidad a DTO (rellena lo que necesites) */
    private FacturaDTO mapperToDTO(Factura f) {
        FacturaDTO dto = new FacturaDTO();
        dto.setId(f.getId());
        dto.setNumeroFactura(f.getNumeroFactura());
        dto.setFechaEmision(f.getFechaEmision());
        dto.setMoneda(f.getMoneda());
        dto.setTipoComprobante(f.getTipoComprobante());
        dto.setFormaPago(f.getFormaPago());
        dto.setMedioPago(f.getMedioPago());
        dto.setSubTotal(f.getSubTotal());
        dto.setTotalImpuestos(f.getTotalImpuestos());
        dto.setTotal(f.getTotal());

        // convertir detalles
        List<FacturaDetalleDTO> detalleDTOs = f.getDetalles().stream().map(d -> {
            FacturaDetalleDTO dd = new FacturaDetalleDTO();
            dd.setDescripcion(d.getDescripcion());
            dd.setCantidad(d.getCantidad());
            dd.setPrecioUnitario(d.getPrecioUnitario());
            dd.setSubtotal(d.getSubtotal());
            dd.setIgv(d.getIgv());
            dd.setTotalLinea(d.getTotalLinea());
            return dd;
        }).toList();
        dto.setItems(detalleDTOs);

        // cliente embebido
        DatosClienteDTO cli = new DatosClienteDTO();
        cli.setId(f.getClienteId());
        cli.setRucDni(f.getRucDniCliente());
        cli.setNombre(f.getNombreCliente());
        dto.setCliente(cli);

        return dto;
    }
}

package org.example.msventa.service.impl;

import org.example.msventa.entity.Venta;
import org.example.msventa.entity.VentaDetalle;
import org.example.msventa.feign.ClienteFeign;
import org.example.msventa.feign.ProductoFeign;
import org.example.msventa.dato.Cliente;
import org.example.msventa.dato.Producto;
import org.example.msventa.repository.VentaRepository;
import org.example.msventa.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired private VentaRepository ventaRepository;
    @Autowired private ClienteFeign clienteFeign;
    @Autowired private ProductoFeign productoFeign;

    @Override
    public List<Venta> listar() {
        List<Venta> ventas = ventaRepository.findAll();
        for (Venta venta : ventas) {
            Cliente cliente = clienteFeign.obtenerPorId(venta.getClienteId()).getBody();
            venta.setCliente(cliente);
            for (VentaDetalle detalle : venta.getDetalles()) {
                Producto producto = productoFeign.obtenerPorId(detalle.getProductoId()).getBody();
                detalle.setProducto(producto);
            }
        }
        return ventas;
    }

    @Override
    public Optional<Venta> obtener(Integer id) {
        Optional<Venta> venta = ventaRepository.findById(id);
        venta.ifPresent(v -> {
            Cliente cliente = clienteFeign.obtenerPorId(v.getClienteId()).getBody();
            v.setCliente(cliente);
            for (VentaDetalle d : v.getDetalles()) {
                Producto producto = productoFeign.obtenerPorId(d.getProductoId()).getBody();
                d.setProducto(producto);
            }
        });
        return venta;
    }
    @Override
    public List<Venta> pendientes(Integer clienteId) {
        return ventaRepository.findByClienteIdAndEstado(clienteId, "SIN_PAGAR");
    }

    @Override
    public void marcarPagada(Integer id) {
        Venta v = ventaRepository.findByIdAndEstado(id, "SIN_PAGAR")
                .orElseThrow(() -> new RuntimeException("Venta no encontrada o ya pagada"));
        v.setEstado("PAGADA");
        ventaRepository.save(v);
    }

    @Override
    public Venta registrar(Venta venta) {

        // 1. validar cliente
        Cliente cliente = clienteFeign.obtenerPorId(venta.getClienteId()).getBody();
        if (cliente == null || !Boolean.TRUE.equals(cliente.getEstado())) {
            throw new RuntimeException("Cliente no válido o inactivo");
        }

        // 2. origen por defecto
        if (venta.getOrigen() == null || venta.getOrigen().isBlank()) {
            venta.setOrigen("CLIENTE");
        }

        // 3. si la venta la crea un trabajador → debe traer trabajadorId
        if ("TRABAJADOR".equalsIgnoreCase(venta.getOrigen()) && venta.getTrabajadorId() == null) {
            throw new RuntimeException("Se requiere trabajadorId para ventas de origen TRABAJADOR");
        }

        // 4. estado inicial → SIN_PAGAR
        venta.setEstado("SIN_PAGAR");
        venta.setFechaVenta(LocalDate.now());

        double total = 0.0;
        for (VentaDetalle detalle : venta.getDetalles()) {

            Producto producto = productoFeign.obtenerPorId(detalle.getProductoId()).getBody();
            if (producto == null || producto.getStock() < detalle.getCantidad()) {
                throw new RuntimeException("Producto sin stock: ID " + detalle.getProductoId());
            }

            // descuento de stock
            //producto.setStock(producto.getStock() - detalle.getCantidad());
            //productoFeign.actualizarProducto(producto.getId(), producto);

            // precios
            detalle.setPrecioUnitario(producto.getPrecioUnitario());
            detalle.setSubtotal(producto.getPrecioUnitario() * detalle.getCantidad());
            total += detalle.getSubtotal();

            // vincular detalle → venta (por cascada)
            detalle.setVenta(venta);
        }

        venta.setTotal(total);
        return ventaRepository.save(venta);
    }

    @Override
    public void eliminar(Integer id) {
        ventaRepository.deleteById(id);
    }
}
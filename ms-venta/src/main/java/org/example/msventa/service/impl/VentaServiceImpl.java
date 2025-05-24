package org.example.msventa.service.impl;

import org.example.msventa.dato.Cliente;
import org.example.msventa.dato.Producto;
import org.example.msventa.entity.Venta;
import org.example.msventa.entity.VentaDetalle;
import org.example.msventa.feign.ClienteFeign;
import org.example.msventa.feign.ProductoFeign;
import org.example.msventa.repository.VentaRepository;
import org.example.msventa.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository repository;

    @Autowired
    private ClienteFeign clienteFeign;

    @Autowired
    private ProductoFeign productoFeign;

    @Override
    public List<Venta> listar() {
        List<Venta> ventas = repository.findAll();
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
        Optional<Venta> ventaOptional = repository.findById(id);
        ventaOptional.ifPresent(venta -> {
            Cliente cliente = clienteFeign.obtenerPorId(venta.getClienteId()).getBody();
            venta.setCliente(cliente);
            for (VentaDetalle detalle : venta.getDetalles()) {
                Producto producto = productoFeign.obtenerPorId(detalle.getProductoId()).getBody();
                detalle.setProducto(producto);
            }
        });
        return ventaOptional;
    }

    @Override
    public Venta registrar(Venta venta) {
        Cliente cliente = clienteFeign.obtenerPorId(venta.getClienteId()).getBody();
        if (cliente == null || !"Activo".equalsIgnoreCase(cliente.getEstado())) {
            throw new RuntimeException("Cliente no válido o inactivo");
        }
        venta.setFechaVenta(LocalDate.now());
        double total = 0.0;
        for (VentaDetalle detalle : venta.getDetalles()) {
            Producto producto = productoFeign.obtenerPorId(detalle.getProductoId()).getBody();
            if (producto == null || producto.getStock() < detalle.getCantidad()) {
                throw new RuntimeException("Producto sin stock suficiente: ID " + detalle.getProductoId());
            }
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoFeign.actualizarProducto(producto.getId(), producto);

            detalle.setPrecioUnitario(producto.getPrecioUnitario());
            double subtotal = detalle.getCantidad() * producto.getPrecioUnitario();
            detalle.setSubtotal(subtotal);
            total += subtotal;
        }
        venta.setTotal(total);
        return repository.save(venta);
    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}
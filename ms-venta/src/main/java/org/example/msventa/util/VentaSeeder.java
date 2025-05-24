package org.example.msventa.util;

import org.example.msventa.entity.Venta;
import org.example.msventa.entity.VentaDetalle;
import org.example.msventa.repository.VentaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class VentaSeeder implements CommandLineRunner {

    private final VentaRepository ventaRepository;

    public VentaSeeder(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    public void run(String... args) {
        if (ventaRepository.count() == 0) {
            // Detalle de productos vendidos
            VentaDetalle d1 = new VentaDetalle();
            d1.setProductoId(1); // Asegúrate de tener este producto
            d1.setCantidad(2);
            d1.setPrecioUnitario(350.0);
            d1.setSubtotal(700.0);

            VentaDetalle d2 = new VentaDetalle();
            d2.setProductoId(2); // Asegúrate de tener este producto
            d2.setCantidad(1);
            d2.setPrecioUnitario(150.0);
            d2.setSubtotal(150.0);

            List<VentaDetalle> detalles = Arrays.asList(d1, d2);

            Venta venta = new Venta();
            venta.setClienteId(1);      // ID de un cliente existente
            venta.setTrabajadorId(1);   // ID del trabajador que registró la venta
            venta.setFechaVenta(LocalDate.now());
            venta.setTotal(850.0);
            venta.setOrigen("TRABAJADOR");
            venta.setEstado("PAGADA");
            venta.setDetalles(detalles);

            ventaRepository.save(venta);

            System.out.println("✅ Venta de ejemplo registrada exitosamente.");
        } else {
            System.out.println("ℹ️ Ya existen ventas registradas, seeder omitido.");
        }
    }
}

package com.example.msproducto.util;

import com.example.msproducto.entity.Producto;
import com.example.msproducto.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class ProductoSeeder implements CommandLineRunner {
    private final ProductoRepository productoRepository;

    public ProductoSeeder(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() == 0) {
            Producto p1 = new Producto(null, "Laptop Lenovo", "Laptop empresarial", "Tecnología",
                    new BigDecimal("3500.00"), 10, 3, "", true, LocalDateTime.now());

            Producto p2 = new Producto(null, "Mouse Logitech", "Mouse inalámbrico", "Accesorios",
                    new BigDecimal("70.00"), 25, 5, "", true, LocalDateTime.now());

            productoRepository.save(p1);
            productoRepository.save(p2);

            System.out.println("Productos insertados correctamente.");
        } else {
            System.out.println("Los productos ya existen.");
        }
    }

}

    package com.example.msproducto.service.impl;

    import com.example.msproducto.entity.Producto;
    import com.example.msproducto.repository.ProductoRepository;
    import com.example.msproducto.service.ProductoService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Optional;

    @Service
    public class ProductoServiceImpl implements ProductoService {

        @Autowired
        private ProductoRepository productoRepository;

        @Override
        public List<Producto> listar() {
            return productoRepository.findAll();
        }

        @Override
        public Optional<Producto> buscar(Long id) {
            return productoRepository.findById(id);
        }

        @Override
        public Producto guardar(Producto producto) {
            if (existePorNombre(producto.getNombre())) {
                throw new RuntimeException("El producto ya está registrado con ese nombre.");
            }
            producto.setFechaCreacion(LocalDateTime.now());
            producto.setEstado(true);
            return productoRepository.save(producto);
        }

        @Override
        public Producto actualizar(Long id, Producto producto) {
            producto.setId(id);
            return productoRepository.save(producto);
        }

        @Override
        public void eliminar(Long id) {
            productoRepository.deleteById(id);
        }

        @Override
        public boolean existePorNombre(String nombre) {
            return productoRepository.existsByNombre(nombre);
        }

    }

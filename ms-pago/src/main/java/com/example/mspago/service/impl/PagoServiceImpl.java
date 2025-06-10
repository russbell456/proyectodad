    package com.example.mspago.service.impl;


    import com.example.mspago.dto.PagoRequest;
    import com.example.mspago.entity.Pago;
    import com.example.mspago.exception.RecursoNoEncontradoException;
    import com.example.mspago.feign.VentaDTO;
    import com.example.mspago.feign.VentaFeign;
    import com.example.mspago.repository.PagoRepository;
    import com.example.mspago.service.PagoService;
    import com.example.mspago.storage.StorageService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;

    import java.time.LocalDateTime;
    import java.util.List;

    @Service
    public class PagoServiceImpl implements PagoService {

        @Autowired private VentaFeign ventaFeign;
        @Autowired private PagoRepository pagoRepository;
        @Autowired private StorageService storageService;

        @Override
        public List<VentaDTO> ventasPendientes(Long clienteId) {
            return ventaFeign.listarPendientes(clienteId).getBody();
        }

        @Override
        public Pago registrar(PagoRequest r, MultipartFile archivo) {

            // 1. obtener la venta y validar
            VentaDTO venta = ventaFeign.obtener(r.getVentaId()).getBody();
            if (venta == null) throw new RecursoNoEncontradoException("Venta no encontrada");
            if (!"SIN_PAGAR".equals(venta.getEstado()))
                throw new RuntimeException("La venta ya está pagada");

            // 2. reglas de método de pago
            if ("TRABAJADOR".equals(venta.getOrigen())
                    && !"CONTADO".equals(r.getMetodo()) && !"TRANSFERENCIA".equals(r.getMetodo())) {
                throw new RuntimeException("Método no permitido para ventas presenciales");
            }

            // 3. manejar comprobante
            String url = null;
            if ("TRANSFERENCIA".equals(r.getMetodo())) {
                if (archivo == null) throw new RuntimeException("Debe adjuntar comprobante");
                url = storageService.store(archivo);
            }

            // 4. guardar pago
            Pago pago = new Pago();
            pago.setVentaId(venta.getId());
            pago.setClienteId(venta.getClienteId());
            pago.setTrabajadorId(r.getTrabajadorId());
            pago.setMetodo(r.getMetodo());
            pago.setMonto(venta.getTotal());
            pago.setFechaPago(LocalDateTime.now());
            pago.setComprobanteUrl(url);

            pagoRepository.save(pago);

            // 5. actualizar venta a PAGADA
            ventaFeign.marcarPagada(venta.getId());

            return pago;
        }

        @Override
        public Pago obtener(Long id) {
            return pagoRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado"));
        }
    }

package org.example.msventa.service.impl;

import org.example.msventa.dato.Cliente;
import org.example.msventa.dato.Producto;
import org.example.msventa.entity.Matricula;
import org.example.msventa.entity.VentaDetalle;
import org.example.msventa.feign.CursoFeign;
import org.example.msventa.feign.EstudianteFeign;
import org.example.msventa.repository.MatriculaRepository;
import org.example.msventa.service.MatriculaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Service
public class MatriculaServiceImpl implements MatriculaService {

    @Autowired
    private MatriculaRepository repository;

    @Autowired
    private EstudianteFeign estudianteFeign;

    @Autowired
    private CursoFeign cursoFeign;

    @Override
    public List<Matricula> listar() {
        List<Matricula> matriculas = repository.findAll();

        for (Matricula matricula : matriculas) {
            Producto producto = estudianteFeign.obtenerPorId(matricula.getEstudianteId()).getBody();
            matricula.setEstudiante(producto);

            for (VentaDetalle detalle : matricula.getDetalles()) {
                Cliente cliente = cursoFeign.obtenerPorId(detalle.getCursoId()).getBody();
                detalle.setCurso(cliente);
            }
        }

        return matriculas;
    }

    @Override
    public Optional<Matricula> obtener(Integer id) {
        Optional<Matricula> optionalMatricula = repository.findById(id);

        optionalMatricula.ifPresent(matricula -> {
            Producto producto = estudianteFeign.obtenerPorId(matricula.getEstudianteId()).getBody();
            matricula.setEstudiante(producto);

            for (VentaDetalle detalle : matricula.getDetalles()) {
                Cliente cliente = cursoFeign.obtenerPorId(detalle.getCursoId()).getBody();
                detalle.setCurso(cliente);
            }
        });

        return optionalMatricula;
    }

    @Override
    public Matricula registrar(Matricula matricula) {
        Producto producto = estudianteFeign.obtenerPorId(matricula.getEstudianteId()).getBody();
        if (producto == null || !"Activo".equals(producto.getEstado())) {
            throw new RuntimeException("Estudiante no válido o inactivo");
        }

        // Verifica si el estudiante ya se matriculó al curso
        List<Matricula> matriculasPrevias = repository.findByEstudianteId(producto.getId());
        for (VentaDetalle detalleNuevo : matricula.getDetalles()) {
            for (Matricula m : matriculasPrevias) {
                for (VentaDetalle detalleExistente : m.getDetalles()) {
                    if (detalleExistente.getCursoId().equals(detalleNuevo.getCursoId())) {
                        throw new RuntimeException("El estudiante ya está matriculado en el curso ID: " + detalleNuevo.getCursoId());
                    }
                }
            }
        }

        matricula.setCiclo(producto.getCicloActual());
        matricula.setFecha(LocalDate.now());

        for (VentaDetalle detalle : matricula.getDetalles()) {
            Cliente cliente = cursoFeign.obtenerPorId(detalle.getCursoId()).getBody();
            if (cliente == null || cliente.getCapacidad() <= 0) {
                throw new RuntimeException("Curso no disponible o sin capacidad");
            }

            // Reduce la capacidad en 1
            cliente.setCapacidad(cliente.getCapacidad() - 1);
            cursoFeign.actualizarCurso(cliente.getId(), cliente);

            detalle.setCurso(cliente);
        }

        return repository.save(matricula);
    }
    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}
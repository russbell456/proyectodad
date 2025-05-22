package org.example.msfacturacion.repository;

import org.example.msfacturacion.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {
    List<Matricula> findByEstudianteId(Integer estudianteId);
}

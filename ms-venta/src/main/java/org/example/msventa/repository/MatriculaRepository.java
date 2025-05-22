package org.example.msventa.repository;

import org.example.msventa.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {
    List<Matricula> findByEstudianteId(Integer estudianteId);
}

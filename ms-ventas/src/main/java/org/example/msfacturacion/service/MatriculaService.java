package org.example.msfacturacion.service;

import org.example.msfacturacion.entity.Matricula;

import java.util.List;
import java.util.Optional;

public interface MatriculaService {
    List<Matricula> listar();
    Optional<Matricula> obtener(Integer id);
    Matricula registrar(Matricula matricula);
    void eliminar(Integer id);
}
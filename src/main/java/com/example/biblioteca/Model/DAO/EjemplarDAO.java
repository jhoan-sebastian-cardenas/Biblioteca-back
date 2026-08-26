package com.example.biblioteca.Model.DAO;

import com.example.biblioteca.Entitys.Ejemplar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EjemplarDAO extends JpaRepository<Ejemplar, Long> {

    List<Ejemplar> findByLibroIsbn(String isbn);
    List<Ejemplar> findByLibroIsbnAndEstadoTrue(String isbn);
}

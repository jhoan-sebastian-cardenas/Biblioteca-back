package com.example.biblioteca.Model.DAO;

import com.example.biblioteca.Entitys.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibroDAO extends JpaRepository<Libro, Long> {

}
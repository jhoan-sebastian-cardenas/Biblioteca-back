package com.example.biblioteca.Model.DAO;

import com.example.biblioteca.Entitys.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioDAO extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);
}

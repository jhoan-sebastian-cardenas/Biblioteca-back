package com.example.biblioteca.Model.DAO;

import com.example.biblioteca.Entitys.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrestamoDAO extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuarioIdUsuario(Long idUsuario);
    List<Prestamo> findByEjemplarLibroIsbn(String isbn);

    boolean existsByUsuarioIdUsuario(Long idUsuario);
    
    boolean existsByEjemplarLibroIdLibro(Long idLibro);

    boolean existsByEjemplarIdEjemplarAndEstadoPrestamo(
            Long idEjemplar,
            Prestamo.EstadoPrestamo estadoPrestamo
    );

    @Query(""" 
        SELECT COUNT (p) > 0 
        FROM Prestamo p
        WHERE p.usuario.idUsuario = :idUsuario
        AND p.fechaDevolucion IS NULL 
        """)
    boolean existsPrestamoPendiente(@Param("idUsuario") Long idUsuario);
}

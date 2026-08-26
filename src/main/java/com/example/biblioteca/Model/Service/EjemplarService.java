package com.example.biblioteca.Model.Service;

import com.example.biblioteca.Entitys.Ejemplar;
import com.example.biblioteca.Entitys.Libro;
import com.example.biblioteca.Exeptions.ResourceNotFoundException;
import com.example.biblioteca.Model.DAO.EjemplarDAO;
import com.example.biblioteca.Model.DAO.LibroDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EjemplarService {

    private final EjemplarDAO ejemplarDAO;
    private final LibroDAO libroDAO;

    public EjemplarService(EjemplarDAO ejemplarDAO, LibroDAO libroDAO) {
        this.ejemplarDAO = ejemplarDAO;
        this.libroDAO = libroDAO;
    }

    public Ejemplar crearEjemplar(Long idLibro, Ejemplar ejemplar) {

        Libro libro = libroDAO.findById(idLibro)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Libro no encontrado"));

        ejemplar.setLibro(libro);

        return ejemplarDAO.save(ejemplar);
    }

    public List<Ejemplar> listarPorIsbn(String isbn) {
       return ejemplarDAO.findByLibroIsbn(isbn);
    }
    public List<Ejemplar> listarDisponiblesPorIsbn(String isbn) {
        return ejemplarDAO.findByLibroIsbnAndEstadoTrue(isbn);
    }
}

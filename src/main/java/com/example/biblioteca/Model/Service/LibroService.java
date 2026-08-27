package com.example.biblioteca.Model.Service;

import com.example.biblioteca.Entitys.Libro;
import com.example.biblioteca.Exeptions.BusinessException;
import com.example.biblioteca.Exeptions.ResourceNotFoundException;
import com.example.biblioteca.Model.DAO.EjemplarDAO;
import com.example.biblioteca.Model.DAO.LibroDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {

    private final LibroDAO libroDAO;
    private final EjemplarDAO ejemplarDAO;

    public LibroService(LibroDAO libroDAO , EjemplarDAO ejemplarDAO) {
        this.libroDAO = libroDAO;
        this.ejemplarDAO = ejemplarDAO;
    }

    public List<Libro> findAll() {
        return libroDAO.findAll();
    }

    public Libro buscarPorId(Long id) {
        return libroDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
    }

    public Libro crear(Libro libro) {
        return libroDAO.save(libro);
    }

    public Libro actualizar(Long id, Libro libro) {

        Libro actualizado = buscarPorId(id);
        actualizado.setTitulo(libro.getTitulo().trim());
        actualizado.setIsbn(libro.getIsbn());
        actualizado.setEdicion(libro.getEdicion());
        actualizado.setAutor(libro.getAutor().trim());
        actualizado.setFechaPublicacion(libro.getFechaPublicacion());

        return libroDAO.save(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        Libro eliminado = buscarPorId(id);

        if (eliminado.getEjemplar() != null && eliminado.getEjemplar().stream()
                .anyMatch(ejemplar -> Boolean.FALSE.equals(ejemplar.getEstado()))) {
            throw new BusinessException("No se puede eliminar el libro porque tiene ejemplares no disponibles.");
        }

        ejemplarDAO.deleteByLibro(eliminado);
        libroDAO.delete(eliminado);
    }
}

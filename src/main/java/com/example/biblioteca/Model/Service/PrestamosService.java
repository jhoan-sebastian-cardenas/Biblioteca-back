package com.example.biblioteca.Model.Service;

import com.example.biblioteca.Entitys.Ejemplar;
import com.example.biblioteca.Entitys.Prestamo;
import com.example.biblioteca.Entitys.Usuario;
import com.example.biblioteca.Exeptions.BusinessException;
import com.example.biblioteca.Exeptions.ResourceNotFoundException;
import com.example.biblioteca.Model.DAO.EjemplarDAO;
import com.example.biblioteca.Model.DAO.PrestamoDAO;
import com.example.biblioteca.Model.DAO.UsuarioDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamosService {

    private final PrestamoDAO prestamoDAO;
    private final UsuarioDAO usuarioDAO;
    private final EjemplarDAO ejemplarDAO;

    public PrestamosService(PrestamoDAO prestamoDAO, UsuarioDAO usuarioDAO, EjemplarDAO ejemplarDAO) {
        this.prestamoDAO = prestamoDAO;
        this.usuarioDAO = usuarioDAO;
        this.ejemplarDAO = ejemplarDAO;
    }

    public List<Prestamo> listarPorUsuario(Long idUsuario) {

        List<Prestamo> prestamos = prestamoDAO.findByUsuarioIdUsuario(idUsuario);

        for(Prestamo prestamo: prestamos) {
            actualizarEstado(prestamo);
        }

        //Actualizar el estado del prestamo del usuario al momento de listar todos sus prestamos
        return prestamoDAO.saveAll(prestamos);
    }

    public List<Prestamo> listarPorIsbn(String isbn) {
        return prestamoDAO.findByEjemplarLibroIsbn(isbn);
    }

    private static final int DIASPRESTAMO = 14;

    @Transactional
    public Prestamo registrarPrestamo(Long idUsuario, Long idEjemplar) {
        Usuario usuario = usuarioDAO.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Ejemplar ejemplar = ejemplarDAO.findById(idEjemplar)
                .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado"));

        //Verificar si el usuario tiene un prestamo Activo
        boolean tienePrestamoPendiente = prestamoDAO
                .existsPrestamoPendiente(idUsuario);
        if (tienePrestamoPendiente) {
            throw new BusinessException("El Usuario tiene un prestamo pendiente para devolver");
        }

        //Verifica si el ejemplar esta disponible
        boolean ejemplarPrestado = prestamoDAO
                .existsByEjemplarIdEjemplarAndEstadoPrestamo(idEjemplar, Prestamo.EstadoPrestamo.ACTIVO);
        if (ejemplarPrestado) {
            throw new BusinessException("El Ejemplar tiene un prestamo ACTIVO");
        }

        LocalDate fechaPrestamo = LocalDate.now();
        LocalDate fechaLimite = fechaPrestamo.plusDays(DIASPRESTAMO);

        // regitrar Prestamo
        Prestamo crearPrestamo = new Prestamo();
        crearPrestamo.setUsuario(usuario);
        crearPrestamo.setEjemplar(ejemplar);
        crearPrestamo.setFechaPrestamo(fechaPrestamo);
        crearPrestamo.setFechaLimite(fechaLimite);
        crearPrestamo.setEstadoPrestamo(Prestamo.EstadoPrestamo.ACTIVO);

        //Ejemplar deja de esta Disponible
        ejemplar.setEstado(false);
        return prestamoDAO.save(crearPrestamo);
    }

    public Prestamo devolverPrestamo(Long idPrestamo) {
        Prestamo prestamo = prestamoDAO.findById(idPrestamo)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado"));

        if (prestamo.getEstadoPrestamo() == Prestamo.EstadoPrestamo.DEVUELTO) {
            throw new BusinessException("El Prestamo ya fue DEVUELTO");
        }

        Ejemplar ejemplar = prestamo.getEjemplar();

        prestamo.setFechaDevolucion(LocalDate.now());
        prestamo.setEstadoPrestamo(Prestamo.EstadoPrestamo.DEVUELTO);

        //El ejemplar Otra vez es Disponible
        ejemplar.setEstado(true);
        ejemplarDAO.save(ejemplar);

        return prestamoDAO.save(prestamo);
    }

    private void actualizarEstado(Prestamo prestamo) {

        LocalDate hoy = LocalDate.now();

        if (prestamo.getFechaDevolucion() != null) {
            prestamo.setEstadoPrestamo(Prestamo.EstadoPrestamo.DEVUELTO);
        } else if (hoy.isAfter(prestamo.getFechaLimite())) {
            prestamo.setEstadoPrestamo(Prestamo.EstadoPrestamo.VENCIDO);
        } else {
            prestamo.setEstadoPrestamo(Prestamo.EstadoPrestamo.ACTIVO);
        }
    }
}



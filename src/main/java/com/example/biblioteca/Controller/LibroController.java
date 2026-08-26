package com.example.biblioteca.Controller;

import com.example.biblioteca.Entitys.Ejemplar;
import com.example.biblioteca.Entitys.Libro;
import com.example.biblioteca.Model.Service.EjemplarService;
import com.example.biblioteca.Model.Service.LibroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/libros")
public class LibroController {

    private LibroService libroService;
    private final EjemplarService ejemplarService;

    public LibroController( LibroService  libroService,  EjemplarService ejemplarService) {
        this.libroService = libroService;
        this.ejemplarService = ejemplarService;
    }

    @GetMapping
    public ResponseEntity<List<Libro>> findAll() {
        return ResponseEntity.ok(libroService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> findById(@PathVariable Long id) {
        return ResponseEntity.ok(libroService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Libro> save(@RequestBody Libro libro) {
        Libro crearLibro = libroService.crear(libro);
        return ResponseEntity.status(HttpStatus.CREATED).body(crearLibro);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Libro> update(@PathVariable Long id, @RequestBody Libro libro) {
        return ResponseEntity.ok(libroService.actualizar(id, libro));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{isbn}/ejemplares")
    public ResponseEntity<List<Ejemplar>> ListarEjemplares(@PathVariable String isbn) {
        return ResponseEntity.ok(ejemplarService.listarPorIsbn(isbn));
    }

    @GetMapping("/{isbn}/ejemplares/disponibles")
    public ResponseEntity<List<Ejemplar>> listarEjemplaresDisponibles(
            @PathVariable String isbn) {

        return ResponseEntity.ok(
                ejemplarService.listarDisponiblesPorIsbn(isbn)
        );
    }
    @PostMapping("/{idLibro}/ejemplares")
    public ResponseEntity<Ejemplar> crearEjemplares(
            @PathVariable Long idLibro,
            @RequestBody Ejemplar ejemplar) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ejemplarService.crearEjemplar(idLibro, ejemplar));
    }
}

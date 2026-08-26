package com.example.biblioteca.Controller;

import com.example.biblioteca.Entitys.Prestamo;
import com.example.biblioteca.DTO.PrestamoRequest;
import com.example.biblioteca.Model.Service.PrestamosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("api/prestamos")
public class PrestamoController {

    private PrestamosService prestamoService;

    public PrestamoController(PrestamosService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @PostMapping
    public ResponseEntity<Prestamo> crearPrestamo(@RequestBody PrestamoRequest prestamoRequest) {
        Prestamo prestamo = prestamoService.registrarPrestamo(
                prestamoRequest.getIdUsuario(),prestamoRequest.getIdEjemplar()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamo);
    }
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Prestamo>> ListarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(prestamoService.listarPorUsuario(idUsuario));
    }
    @GetMapping("/libro/{isbn}")
    public ResponseEntity<List<Prestamo>> ListarPorIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(prestamoService.listarPorIsbn(isbn));
    }

    @PutMapping("/{idPrestamo}/devolver")
    public ResponseEntity<Prestamo> devolverPrestamo(@PathVariable Long idPrestamo) {
        return ResponseEntity.ok(prestamoService.devolverPrestamo(idPrestamo));
    }

}

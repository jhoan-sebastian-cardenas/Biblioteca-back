package com.example.biblioteca.Entitys;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  idLibro;

    @NotNull
    @Size(min = 1,max = 200)
    @Column( name = "titulo")
    private String titulo;

    @NotNull
    @Size(min =1, max = 100)
    @Column(name = "isbn")
    private String isbn;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "edicion")
    private String edicion;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "autor")
    private String autor;

    @NotNull
    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;

    @OneToMany(mappedBy = "libro")
    private List<Ejemplar> ejemplares;

    public long getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(long idLibro) {
        this.idLibro = idLibro;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getEdicion() {
        return edicion;
    }

    public void setEdicion(String edicion) {
        this.edicion = edicion;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public List<Ejemplar> getEjemplar() {
        return ejemplares;
    }

    public void setEjemplar(List<Ejemplar> ejemplar) {
        this.ejemplares = ejemplar;
    }

}

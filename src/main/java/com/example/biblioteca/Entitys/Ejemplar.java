package com.example.biblioteca.Entitys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "ejemplares")
public class Ejemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEjemplar;

    @NotNull
    @Column(name = "estado")
    private Boolean estado;

    @ManyToOne
    @JoinColumn(name = "idLibro")
    @JsonIgnore
    private Libro libro;

    @OneToMany(mappedBy = "ejemplar")
    @JsonIgnore
    private List<Prestamo> prestamos;


    public long getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(long idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public List<Prestamo> getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(List<Prestamo> prestamos) {
        this.prestamos = prestamos;
    }
}

package com.example.biblioteca.Model.Service;

import com.example.biblioteca.Entitys.Usuario;
import com.example.biblioteca.Exeptions.ResourceNotFoundException;
import com.example.biblioteca.Model.DAO.UsuarioDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public List<Usuario> findAll() {
        return usuarioDAO.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public Usuario crear(Usuario usuario) {
       if (usuarioDAO.existsByEmail(usuario.getEmail())) {
           throw new ResourceNotFoundException("Este Email ya existe");
       }
       return usuarioDAO.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario usuario) {

        Usuario actualizarusuario = buscarPorId(id);
        actualizarusuario.setNombre(usuario.getNombre().trim());
        actualizarusuario.setApellido(usuario.getApellido().trim());
        actualizarusuario.setEmail(usuario.getEmail().trim());
        actualizarusuario.setFechaNacimiento(usuario.getFechaNacimiento());
        return usuarioDAO.save(actualizarusuario);
    }
    public void eliminar(Long id) {

        Usuario usuario = buscarPorId(id);

        usuarioDAO.delete(usuario);
    }
}

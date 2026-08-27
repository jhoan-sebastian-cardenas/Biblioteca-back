package com.example.biblioteca.Model.Service;

import com.example.biblioteca.Entitys.Usuario;
import com.example.biblioteca.Exeptions.BusinessException;
import com.example.biblioteca.Exeptions.ResourceNotFoundException;
import com.example.biblioteca.Model.DAO.PrestamoDAO;
import com.example.biblioteca.Model.DAO.UsuarioDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final PrestamoDAO prestamoDAO;

    public UsuarioService(UsuarioDAO usuarioDAO, PrestamoDAO prestamoDAO) {
        this.usuarioDAO = usuarioDAO;
        this.prestamoDAO = prestamoDAO;
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
    @Transactional
    public void eliminar(Long id) {
        buscarPorId(id);

        if (prestamoDAO.existsByUsuarioIdUsuario(id)) {
            throw new BusinessException(
                    "No se puede eliminar el usuario porque tiene historial de préstamos."
            );
        }

        usuarioDAO.deleteById(id);
    }
}

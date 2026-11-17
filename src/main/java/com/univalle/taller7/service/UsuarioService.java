package com.univalle.taller7.service;

import com.univalle.taller7.dto.UsuarioRequest;
import com.univalle.taller7.dto.UsuarioResponse;
import com.univalle.taller7.entity.Usuario;
import com.univalle.taller7.exception.EmailDuplicadoException;
import com.univalle.taller7.exception.UsuarioNoEncontradoException;
import com.univalle.taller7.repository.UsuarioRepository;
import java.util.Locale;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);

        if (repository.existsByEmail(email)) {
            throw new EmailDuplicadoException();
        }

        String nombre = request.nombre().strip();

        try {
            Usuario usuario = new Usuario(nombre, email);
            Usuario usuarioGuardado = repository.saveAndFlush(usuario);
            return convertirAResponse(usuarioGuardado);
        } catch (DataIntegrityViolationException ex) {
            // Otro POST puede guardar el mismo email mientras se procesa este.
            if (esEmailDuplicado(ex)) {
                throw new EmailDuplicadoException();
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        Optional<Usuario> resultado = repository.findById(id);

        if (resultado.isEmpty()) {
            throw new UsuarioNoEncontradoException();
        }

        Usuario usuario = resultado.get();
        return convertirAResponse(usuario);
    }

    private boolean esEmailDuplicado(Throwable error) {
        Throwable causa = error;

        while (causa != null) {
            if (causa instanceof ConstraintViolationException) {
                ConstraintViolationException restriccion = (ConstraintViolationException) causa;
                return "uk_usuarios_email".equals(restriccion.getConstraintName());
            }
            causa = causa.getCause();
        }

        return false;
    }

    private UsuarioResponse convertirAResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail());
    }
}

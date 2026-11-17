package com.univalle.taller7;

import com.univalle.taller7.dto.UsuarioRequest;
import com.univalle.taller7.dto.UsuarioResponse;
import com.univalle.taller7.entity.Usuario;
import com.univalle.taller7.exception.EmailDuplicadoException;
import com.univalle.taller7.exception.UsuarioNoEncontradoException;
import com.univalle.taller7.repository.UsuarioRepository;
import com.univalle.taller7.service.UsuarioService;
import java.sql.SQLException;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioService service;

    @Test
    void creaYNormaliza() {
        Usuario guardado = new Usuario("Juan", "juan@example.com");
        when(repository.saveAndFlush(any(Usuario.class))).thenReturn(guardado);

        UsuarioRequest request = new UsuarioRequest(" Juan ", "JUAN@example.com");
        UsuarioResponse resultado = service.crearUsuario(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).saveAndFlush(captor.capture());
        Usuario enviado = captor.getValue();
        assertEquals("Juan", enviado.getNombre());
        assertEquals("juan@example.com", enviado.getEmail());
        assertEquals("Juan", resultado.nombre());
        assertEquals("juan@example.com", resultado.email());
    }

    @Test
    void consulta() {
        Usuario usuario = new Usuario("Juan", "j@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponse resultado = service.obtenerUsuarioPorId(1L);

        assertEquals("Juan", resultado.nombre());
        assertEquals("j@example.com", resultado.email());
    }

    @Test
    void inexistente() {
        assertThrows(UsuarioNoEncontradoException.class,
                () -> service.obtenerUsuarioPorId(99L));
    }

    @Test
    void duplicado() {
        when(repository.existsByEmail("j@example.com")).thenReturn(true);
        UsuarioRequest request = new UsuarioRequest("Juan", "j@example.com");

        assertThrows(EmailDuplicadoException.class, () -> service.crearUsuario(request));

        verify(repository, never()).saveAndFlush(any(Usuario.class));
    }

    @Test
    void duplicadoConcurrente() {
        SQLException errorSql = new SQLException("duplicate", "23505");
        ConstraintViolationException restriccion = new ConstraintViolationException(
                "unique", errorSql, "uk_usuarios_email");
        DataIntegrityViolationException error = new DataIntegrityViolationException(
                "duplicate", restriccion);
        when(repository.saveAndFlush(any(Usuario.class))).thenThrow(error);
        UsuarioRequest request = new UsuarioRequest("Juan", "j@example.com");

        assertThrows(EmailDuplicadoException.class, () -> service.crearUsuario(request));
    }

    @Test
    void noOcultaOtrasRestricciones() {
        DataIntegrityViolationException error = new DataIntegrityViolationException("otro error");
        when(repository.saveAndFlush(any(Usuario.class))).thenThrow(error);
        UsuarioRequest request = new UsuarioRequest("Juan", "j@example.com");

        DataIntegrityViolationException resultado = assertThrows(
                DataIntegrityViolationException.class, () -> service.crearUsuario(request));

        assertSame(error, resultado);
    }

    @Test
    void otraRestriccionSqlNoSeConfundeConEmailDuplicado() {
        SQLException errorSql = new SQLException("duplicate", "23505");
        ConstraintViolationException restriccion = new ConstraintViolationException(
                "unique", errorSql, "usuarios_pkey");
        DataIntegrityViolationException error = new DataIntegrityViolationException(
                "otra restriccion", restriccion);
        when(repository.saveAndFlush(any(Usuario.class))).thenThrow(error);
        UsuarioRequest request = new UsuarioRequest("Juan", "j@example.com");

        DataIntegrityViolationException resultado = assertThrows(
                DataIntegrityViolationException.class, () -> service.crearUsuario(request));

        assertSame(error, resultado);
    }
}

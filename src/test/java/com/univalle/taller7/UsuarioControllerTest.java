package com.univalle.taller7;

import com.univalle.taller7.controller.UsuarioController;
import com.univalle.taller7.dto.UsuarioResponse;
import com.univalle.taller7.exception.EmailDuplicadoException;
import com.univalle.taller7.exception.UsuarioNoEncontradoException;
import com.univalle.taller7.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UsuarioService service;

    @Test
    void crea() throws Exception {
        UsuarioResponse usuario = new UsuarioResponse(1L, "Juan", "j@example.com");
        when(service.crearUsuario(any())).thenReturn(usuario);
        String body = """
                {"nombre":"Juan","email":"j@example.com"}
                """;

        mvc.perform(post("/api/usuarios").contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/usuarios/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("j@example.com"));
    }

    @Test
    void consulta() throws Exception {
        UsuarioResponse usuario = new UsuarioResponse(1L, "Juan", "j@example.com");
        when(service.obtenerUsuarioPorId(1L)).thenReturn(usuario);

        mvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void inexistente() throws Exception {
        when(service.obtenerUsuarioPorId(99L)).thenThrow(new UsuarioNoEncontradoException());

        mvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Usuario no encontrado"))
                .andExpect(jsonPath("$.instance").value("/api/usuarios/99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void datosInvalidos() throws Exception {
        String body = """
                {"nombre":" ","email":"invalido"}
                """;

        mvc.perform(post("/api/usuarios").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.errors.email").exists());
        verifyNoInteractions(service);
    }

    @Test
    void duplicado() throws Exception {
        when(service.crearUsuario(any())).thenThrow(new EmailDuplicadoException());
        String body = """
                {"nombre":"Juan","email":"j@example.com"}
                """;

        mvc.perform(post("/api/usuarios").contentType("application/json").content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("El email ya esta registrado"));
    }

    @Test
    void jsonMalformado() throws Exception {
        mvc.perform(post("/api/usuarios").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void idInvalido() throws Exception {
        mvc.perform(get("/api/usuarios/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void camposAusentes() throws Exception {
        mvc.perform(post("/api/usuarios").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.errors.email").exists());
        verifyNoInteractions(service);
    }

    @Test
    void bodyNulo() throws Exception {
        mvc.perform(post("/api/usuarios").contentType("application/json").content("null"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void nombreDemasiadoLargo() throws Exception {
        String nombre = "a".repeat(101);
        String body = "{\"nombre\":\"" + nombre + "\",\"email\":\"j@example.com\"}";

        mvc.perform(post("/api/usuarios").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").exists());
        verifyNoInteractions(service);
    }

    @Test
    void idFueraDelRangoLong() throws Exception {
        mvc.perform(get("/api/usuarios/9223372036854775808"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}

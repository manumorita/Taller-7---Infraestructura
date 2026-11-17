package com.univalle.taller7;

import com.univalle.taller7.dto.UsuarioRequest;
import com.univalle.taller7.dto.UsuarioResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsuarioPostgresIT {

    @Container
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:11")
            .withDatabaseName("demo");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", db::getJdbcUrl);
        registry.add("spring.datasource.username", db::getUsername);
        registry.add("spring.datasource.password", db::getPassword);
    }

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void creaYConsultaEnPostgres() {
        UsuarioRequest request = new UsuarioRequest(" Juan Perez ", "JUAN@example.com");

        ResponseEntity<UsuarioResponse> creacion = http.postForEntity(
                "/api/usuarios", request, UsuarioResponse.class);

        assertEquals(HttpStatus.CREATED, creacion.getStatusCode());
        assertNotNull(creacion.getHeaders().getLocation());
        UsuarioResponse usuario = creacion.getBody();
        assertNotNull(usuario);
        assertNotNull(usuario.id());
        assertEquals("Juan Perez", usuario.nombre());
        assertEquals("juan@example.com", usuario.email());

        ResponseEntity<UsuarioResponse> consulta = http.getForEntity(
                "/api/usuarios/" + usuario.id(), UsuarioResponse.class);

        assertEquals(HttpStatus.OK, consulta.getStatusCode());
        assertEquals(usuario, consulta.getBody());
        Integer cantidad = jdbc.queryForObject(
                "select count(*) from usuarios where id=?", Integer.class, usuario.id());
        assertEquals(1, cantidad);
    }

    @Test
    void flywayAplicaLaMigracion() {
        Integer migraciones = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success=true", Integer.class);
        assertEquals(1, migraciones);
    }

    @Test
    void rechazaEmailDuplicadoAunqueCambienLasMayusculas() {
        UsuarioRequest request = new UsuarioRequest("Ana", "ana@example.com");
        ResponseEntity<String> creacion = http.postForEntity(
                "/api/usuarios", request, String.class);
        assertEquals(HttpStatus.CREATED, creacion.getStatusCode());

        ResponseEntity<String> duplicado = http.postForEntity(
                "/api/usuarios", request, String.class);
        assertEquals(HttpStatus.CONFLICT, duplicado.getStatusCode());

        UsuarioRequest mayusculas = new UsuarioRequest("Otra persona", "ANA@example.com");
        ResponseEntity<String> otroDuplicado = http.postForEntity(
                "/api/usuarios", mayusculas, String.class);
        assertEquals(HttpStatus.CONFLICT, otroDuplicado.getStatusCode());
    }

    @Test
    void usuarioInexistente() {
        ResponseEntity<String> respuesta = http.getForEntity(
                "/api/usuarios/999999", String.class);
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void rechazaDatosInvalidos() {
        UsuarioRequest request = new UsuarioRequest(" ", "bad");
        ResponseEntity<String> respuesta = http.postForEntity(
                "/api/usuarios", request, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void documentacionYSalud() {
        String[] rutas = {
            "/api/demo/actuator/health",
            "/api/demo/actuator/health/readiness",
            "/api/demo/actuator/health/liveness",
            "/v3/api-docs",
            "/swagger-ui/index.html"
        };

        for (String ruta : rutas) {
            ResponseEntity<String> respuesta = http.getForEntity(ruta, String.class);
            assertEquals(HttpStatus.OK, respuesta.getStatusCode(), ruta);
        }

        String documento = http.getForObject("/v3/api-docs", String.class);
        assertNotNull(documento);
        assertTrue(documento.contains("/api/usuarios"));
    }
}

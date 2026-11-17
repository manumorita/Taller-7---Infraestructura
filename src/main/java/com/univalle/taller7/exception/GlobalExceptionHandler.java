package com.univalle.taller7.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    ResponseEntity<Object> usuarioNoEncontrado(UsuarioNoEncontradoException ex,
                                              HttpServletRequest request) {
        return crearRespuestaError(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    ResponseEntity<Object> emailDuplicado(EmailDuplicadoException ex,
                                         HttpServletRequest request) {
        return crearRespuestaError(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    private ResponseEntity<Object> crearRespuestaError(String mensaje, HttpStatus estado,
                                                       HttpServletRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, mensaje);
        problema.setInstance(URI.create(request.getRequestURI()));
        problema.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(estado).body(problema);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        Map<String, String> errores = new TreeMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                status, "Los datos enviados no son validos");
        problema.setProperty("errors", errores);
        return handleExceptionInternal(ex, problema, headers, status, request);
    }
}

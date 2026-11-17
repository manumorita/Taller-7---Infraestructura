package com.univalle.taller7.exception;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException() {
        super("Usuario no encontrado");
    }
}

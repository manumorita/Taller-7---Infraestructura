package com.univalle.taller7.exception;

public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException() {
        super("El email ya esta registrado");
    }
}

package com.leonardo.bankapi.exception;

/**
 * Lançada quando um recurso não é encontrado.
 * Mapeada para HTTP 404 pelo GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

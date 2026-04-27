package com.leonardo.bankapi.exception;

/**
 * Lançada quando uma regra de negócio é violada.
 * Mapeada para HTTP 422 Unprocessable Entity pelo GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

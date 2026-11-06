package br.com.senac.organizacaorecursos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Order(org.springframework.core.Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> recursoNaoEncontrado(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, "Recurso nao encontrado.", request));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> erroInterno(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.", request));
    }
}

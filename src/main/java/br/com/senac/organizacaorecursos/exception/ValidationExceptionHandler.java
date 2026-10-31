package br.com.senac.organizacaorecursos.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ValidationExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Map<String, String>> parametrosInvalidos() {
        return ResponseEntity.badRequest().body(Map.of("message", "Parametros invalidos."));
    }
}

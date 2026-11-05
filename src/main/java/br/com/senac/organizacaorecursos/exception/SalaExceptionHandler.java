package br.com.senac.organizacaorecursos.exception;

import br.com.senac.organizacaorecursos.service.SalaNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class SalaExceptionHandler {
    @ExceptionHandler(SalaNotFoundException.class)
    ResponseEntity<ApiError> salaNaoEncontrada(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, "Sala nao encontrada.", request));
    }
}

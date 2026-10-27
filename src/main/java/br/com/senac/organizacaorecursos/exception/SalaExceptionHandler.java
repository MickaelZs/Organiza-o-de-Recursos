package br.com.senac.organizacaorecursos.exception;

import br.com.senac.organizacaorecursos.service.SalaNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SalaExceptionHandler {
    @ExceptionHandler(SalaNotFoundException.class)
    ResponseEntity<Map<String, String>> salaNaoEncontrada() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Sala nao encontrada."));
    }
}

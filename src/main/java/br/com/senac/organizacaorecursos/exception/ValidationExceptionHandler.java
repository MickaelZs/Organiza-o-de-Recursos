package br.com.senac.organizacaorecursos.exception;

import jakarta.validation.ConstraintViolationException;
import br.com.senac.organizacaorecursos.service.IntervaloDisponibilidadeInvalidoException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class ValidationExceptionHandler {
    @ExceptionHandler(IntervaloDisponibilidadeInvalidoException.class)
    ResponseEntity<Map<String, String>> intervaloDisponibilidadeInvalido() {
        return ResponseEntity.badRequest().body(Map.of("message", "Informe um intervalo valido com termino posterior ao inicio."));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Map<String, String>> parametrosInvalidos() {
        return ResponseEntity.badRequest().body(Map.of("message", "Parametros invalidos."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> corpoInvalido() {
        return ResponseEntity.badRequest().body(Map.of("message", "Requisicao invalida."));
    }
}

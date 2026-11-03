package br.com.senac.organizacaorecursos.exception;

import br.com.senac.organizacaorecursos.service.ReservaNotFoundException;
import br.com.senac.organizacaorecursos.service.ReservaConflitoException;
import br.com.senac.organizacaorecursos.service.ReservaOwnershipException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReservaExceptionHandler {
    @ExceptionHandler(ReservaNotFoundException.class)
    ResponseEntity<Map<String, String>> reservaNaoEncontrada() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Reserva nao encontrada."));
    }

    @ExceptionHandler(ReservaOwnershipException.class)
    ResponseEntity<Map<String, String>> acessoNegado() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acesso a reserva nao permitido."));
    }

    @ExceptionHandler(ReservaConflitoException.class)
    ResponseEntity<Map<String, String>> conflitoDeReserva() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Horario indisponivel para o recurso."));
    }
}

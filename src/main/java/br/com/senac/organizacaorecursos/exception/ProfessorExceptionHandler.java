package br.com.senac.organizacaorecursos.exception;

import br.com.senac.organizacaorecursos.service.DuplicateProfessorEmailException;
import br.com.senac.organizacaorecursos.service.ProfessorNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProfessorExceptionHandler {
    @ExceptionHandler(ProfessorNotFoundException.class)
    ResponseEntity<Map<String, String>> professorNaoEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Professor nao encontrado."));
    }

    @ExceptionHandler(DuplicateProfessorEmailException.class)
    ResponseEntity<Map<String, String>> emailDuplicado() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "E-mail de professor ja cadastrado."));
    }
}

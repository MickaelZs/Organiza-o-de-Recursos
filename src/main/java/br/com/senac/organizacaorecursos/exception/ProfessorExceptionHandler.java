package br.com.senac.organizacaorecursos.exception;

import br.com.senac.organizacaorecursos.service.DuplicateProfessorEmailException;
import br.com.senac.organizacaorecursos.service.ProfessorNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class ProfessorExceptionHandler {
    @ExceptionHandler(ProfessorNotFoundException.class)
    ResponseEntity<ApiError> professorNaoEncontrado(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, "Professor nao encontrado.", request));
    }

    @ExceptionHandler(DuplicateProfessorEmailException.class)
    ResponseEntity<ApiError> emailDuplicado(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT, "E-mail de professor ja cadastrado.", request));
    }
}

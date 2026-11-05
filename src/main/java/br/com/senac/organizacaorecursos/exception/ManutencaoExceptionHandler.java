package br.com.senac.organizacaorecursos.exception;

import br.com.senac.organizacaorecursos.service.ManutencaoConflitoException;
import br.com.senac.organizacaorecursos.service.ManutencaoNotFoundException;
import br.com.senac.organizacaorecursos.service.ManutencaoOwnershipException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class ManutencaoExceptionHandler {
    @ExceptionHandler(ManutencaoConflitoException.class)
    ResponseEntity<ApiError> conflitoDeManutencao(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT, "Horario indisponivel para o recurso.", request));
    }

    @ExceptionHandler(ManutencaoNotFoundException.class)
    ResponseEntity<ApiError> manutencaoNaoEncontrada(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, "Manutencao nao encontrada.", request));
    }

    @ExceptionHandler(ManutencaoOwnershipException.class)
    ResponseEntity<ApiError> acessoNegado(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(HttpStatus.FORBIDDEN, "Acesso a manutencao nao permitido.", request));
    }
}

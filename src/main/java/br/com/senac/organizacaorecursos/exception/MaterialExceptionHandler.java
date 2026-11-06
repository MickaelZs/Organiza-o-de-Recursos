package br.com.senac.organizacaorecursos.exception;

import br.com.senac.organizacaorecursos.service.InvalidMaterialQuantityException;
import br.com.senac.organizacaorecursos.service.MaterialNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class MaterialExceptionHandler {
    @ExceptionHandler(MaterialNotFoundException.class)
    ResponseEntity<ApiError> materialNaoEncontrado(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, "Material nao encontrado.", request));
    }

    @ExceptionHandler(InvalidMaterialQuantityException.class)
    ResponseEntity<ApiError> quantidadeInvalida(HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Quantidade disponivel invalida.", request));
    }
}

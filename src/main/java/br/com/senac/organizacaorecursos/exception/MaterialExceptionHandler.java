package br.com.senac.organizacaorecursos.exception;

import br.com.senac.organizacaorecursos.service.InvalidMaterialQuantityException;
import br.com.senac.organizacaorecursos.service.MaterialNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MaterialExceptionHandler {
    @ExceptionHandler(MaterialNotFoundException.class)
    ResponseEntity<Map<String, String>> materialNaoEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Material nao encontrado."));
    }

    @ExceptionHandler(InvalidMaterialQuantityException.class)
    ResponseEntity<Map<String, String>> quantidadeInvalida() {
        return ResponseEntity.badRequest().body(Map.of("message", "Quantidade disponivel invalida."));
    }
}

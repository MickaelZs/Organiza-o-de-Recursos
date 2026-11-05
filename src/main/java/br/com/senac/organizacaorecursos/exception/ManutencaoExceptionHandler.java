package br.com.senac.organizacaorecursos.exception;

import br.com.senac.organizacaorecursos.service.ManutencaoConflitoException;
import br.com.senac.organizacaorecursos.service.ManutencaoNotFoundException;
import br.com.senac.organizacaorecursos.service.ManutencaoOwnershipException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManutencaoExceptionHandler {
    @ExceptionHandler(ManutencaoConflitoException.class)
    ResponseEntity<Map<String, String>> conflitoDeManutencao() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Horario indisponivel para o recurso."));
    }

    @ExceptionHandler(ManutencaoNotFoundException.class)
    ResponseEntity<Map<String, String>> manutencaoNaoEncontrada() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Manutencao nao encontrada."));
    }

    @ExceptionHandler(ManutencaoOwnershipException.class)
    ResponseEntity<Map<String, String>> acessoNegado() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acesso a manutencao nao permitido."));
    }
}

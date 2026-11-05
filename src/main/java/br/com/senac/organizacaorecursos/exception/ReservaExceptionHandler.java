package br.com.senac.organizacaorecursos.exception;

import br.com.senac.organizacaorecursos.service.ReservaNotFoundException;
import br.com.senac.organizacaorecursos.service.ReservaAprovacaoNaoPermitidaException;
import br.com.senac.organizacaorecursos.service.ReservaConflitoException;
import br.com.senac.organizacaorecursos.service.ReservaEstadoInvalidoException;
import br.com.senac.organizacaorecursos.service.ReservaOwnershipException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class ReservaExceptionHandler {
    @ExceptionHandler(ReservaAprovacaoNaoPermitidaException.class)
    ResponseEntity<ApiError> aprovacaoNaoPermitida(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(HttpStatus.FORBIDDEN,
                        "Somente responsaveis podem aprovar ou rejeitar reservas.", request));
    }

    @ExceptionHandler(ReservaEstadoInvalidoException.class)
    ResponseEntity<ApiError> estadoInvalido(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT, "A reserva nao esta pendente para aprovacao.", request));
    }

    @ExceptionHandler(ReservaNotFoundException.class)
    ResponseEntity<ApiError> reservaNaoEncontrada(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, "Reserva nao encontrada.", request));
    }

    @ExceptionHandler(ReservaOwnershipException.class)
    ResponseEntity<ApiError> acessoNegado(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(HttpStatus.FORBIDDEN, "Acesso a reserva nao permitido.", request));
    }

    @ExceptionHandler(ReservaConflitoException.class)
    ResponseEntity<ApiError> conflitoDeReserva(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT, "Horario indisponivel para o recurso.", request));
    }
}

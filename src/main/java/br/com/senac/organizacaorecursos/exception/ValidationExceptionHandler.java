package br.com.senac.organizacaorecursos.exception;

import jakarta.validation.ConstraintViolationException;
import br.com.senac.organizacaorecursos.service.IntervaloDisponibilidadeInvalidoException;
import br.com.senac.organizacaorecursos.service.PeriodoRelatorioInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class ValidationExceptionHandler {
    @ExceptionHandler(IntervaloDisponibilidadeInvalidoException.class)
    ResponseEntity<ApiError> intervaloDisponibilidadeInvalido(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST,
                "Informe um intervalo valido com termino posterior ao inicio.", request));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> parametrosInvalidos(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, "Parametros invalidos.", request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> corpoInvalido(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, "Requisicao invalida.", request));
    }

    @ExceptionHandler(PeriodoRelatorioInvalidoException.class)
    ResponseEntity<ApiError> periodoRelatorioInvalido(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST,
                "O periodo deve ter termino posterior ao inicio.", request));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiError> requisicaoMalformada(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, "Requisicao invalida.", request));
    }
}

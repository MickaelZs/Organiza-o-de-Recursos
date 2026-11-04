package br.com.senac.organizacaorecursos.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record ReservaRequest(
        Long salaId,
        Long professorId,
        Long materialId,
        @NotNull OffsetDateTime inicio,
        @NotNull OffsetDateTime termino) {

    @AssertTrue(message = "Informe exatamente um recurso")
    public boolean isRecursoValido() {
        int quantidade = (salaId == null ? 0 : 1)
                + (professorId == null ? 0 : 1)
                + (materialId == null ? 0 : 1);
        return quantidade == 1;
    }

    @AssertTrue(message = "O termino deve ser posterior ao inicio")
    public boolean isIntervaloValido() {
        return inicio == null || termino == null || termino.isAfter(inicio);
    }
}

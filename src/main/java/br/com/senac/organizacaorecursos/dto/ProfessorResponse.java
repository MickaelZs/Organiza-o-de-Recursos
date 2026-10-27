package br.com.senac.organizacaorecursos.dto;

import br.com.senac.organizacaorecursos.entity.Professor;

public record ProfessorResponse(Long id, String nome, String email, String competencia, Boolean ativo) {
    public static ProfessorResponse from(Professor professor) {
        return new ProfessorResponse(professor.getId(), professor.getNome(), professor.getEmail(),
                professor.getCompetencia(), professor.getAtivo());
    }
}

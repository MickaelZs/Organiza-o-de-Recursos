package br.com.senac.organizacaorecursos.dto;

import br.com.senac.organizacaorecursos.entity.Sala;

public record SalaResponse(Long id, String nome, String tipo, Integer capacidade,
                           String localizacao, Boolean restrita, Boolean ativa) {
    public static SalaResponse from(Sala sala) {
        return new SalaResponse(sala.getId(), sala.getNome(), sala.getTipo(), sala.getCapacidade(),
                sala.getLocalizacao(), sala.getRestrita(), sala.getAtiva());
    }
}

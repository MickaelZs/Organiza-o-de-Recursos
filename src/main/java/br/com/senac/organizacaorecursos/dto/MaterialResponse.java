package br.com.senac.organizacaorecursos.dto;

import br.com.senac.organizacaorecursos.entity.Material;

public record MaterialResponse(Long id, String nome, String tipo, Integer quantidadeTotal,
                               Integer quantidadeDisponivel, String localizacao, Boolean restrito, Boolean ativo) {
    public static MaterialResponse from(Material material) {
        return new MaterialResponse(material.getId(), material.getNome(), material.getTipo(),
                material.getQuantidadeTotal(), material.getQuantidadeDisponivel(), material.getLocalizacao(),
                material.getRestrito(), material.getAtivo());
    }
}

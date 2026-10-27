package br.com.senac.organizacaorecursos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "salas")
public class Sala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String tipo;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer capacidade;

    @NotBlank
    @Column(nullable = false)
    private String localizacao;

    @NotNull
    @Column(nullable = false)
    private Boolean restrita;

    @NotNull
    @Column(nullable = false)
    private Boolean ativa;

    protected Sala() { }

    public Sala(String nome, String tipo, Integer capacidade, String localizacao, Boolean restrita, Boolean ativa) {
        this.nome = nome;
        this.tipo = tipo;
        this.capacidade = capacidade;
        this.localizacao = localizacao;
        this.restrita = restrita;
        this.ativa = ativa;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public Integer getCapacidade() { return capacidade; }
    public String getLocalizacao() { return localizacao; }
    public Boolean getRestrita() { return restrita; }
    public Boolean getAtiva() { return ativa; }
}

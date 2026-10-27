package br.com.senac.organizacaorecursos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "materiais")
public class Material {
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
    @Min(0)
    @Column(nullable = false)
    private Integer quantidadeTotal;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer quantidadeDisponivel;

    @NotBlank
    @Column(nullable = false)
    private String localizacao;

    @NotNull
    @Column(nullable = false)
    private Boolean restrito;

    @NotNull
    @Column(nullable = false)
    private Boolean ativo;

    protected Material() { }

    public Material(String nome, String tipo, Integer quantidadeTotal, Integer quantidadeDisponivel,
                    String localizacao, Boolean restrito, Boolean ativo) {
        this.nome = nome;
        this.tipo = tipo;
        this.quantidadeTotal = quantidadeTotal;
        this.quantidadeDisponivel = quantidadeDisponivel;
        this.localizacao = localizacao;
        this.restrito = restrito;
        this.ativo = ativo;
    }

    @AssertTrue(message = "A quantidade disponivel nao pode superar a quantidade total")
    public boolean isQuantidadeDisponivelValida() {
        return quantidadeTotal == null || quantidadeDisponivel == null || quantidadeDisponivel <= quantidadeTotal;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public Integer getQuantidadeTotal() { return quantidadeTotal; }
    public Integer getQuantidadeDisponivel() { return quantidadeDisponivel; }
    public String getLocalizacao() { return localizacao; }
    public Boolean getRestrito() { return restrito; }
    public Boolean getAtivo() { return ativo; }
}

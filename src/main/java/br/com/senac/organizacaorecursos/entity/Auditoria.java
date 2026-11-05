package br.com.senac.organizacaorecursos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "auditorias")
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @Column(name = "tipo_entidade", nullable = false, length = 40)
    private String tipoEntidade;

    @NotNull
    @Column(name = "entidade_id", nullable = false)
    private Long entidadeId;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String acao;

    @Column(name = "estado_anterior")
    private String estadoAnterior;

    @Column(name = "estado_posterior")
    private String estadoPosterior;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @NotNull
    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    protected Auditoria() { }

    public Auditoria(Usuario usuario, String tipoEntidade, Long entidadeId, String acao,
                     String estadoAnterior, String estadoPosterior, String detalhes, OffsetDateTime criadoEm) {
        this.usuario = usuario;
        this.tipoEntidade = tipoEntidade;
        this.entidadeId = entidadeId;
        this.acao = acao;
        this.estadoAnterior = estadoAnterior;
        this.estadoPosterior = estadoPosterior;
        this.detalhes = detalhes;
        this.criadoEm = criadoEm;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getTipoEntidade() { return tipoEntidade; }
    public Long getEntidadeId() { return entidadeId; }
    public String getAcao() { return acao; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoPosterior() { return estadoPosterior; }
    public String getDetalhes() { return detalhes; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
}

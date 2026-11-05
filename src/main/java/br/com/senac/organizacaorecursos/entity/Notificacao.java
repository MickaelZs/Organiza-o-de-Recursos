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
@Table(name = "notificacoes")
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @NotBlank
    @Column(nullable = false, length = 60)
    private String tipo;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @NotNull
    @Column(name = "criada_em", nullable = false)
    private OffsetDateTime criadaEm;

    @NotNull
    @Column(nullable = false)
    private Boolean lida;

    @Column(name = "lida_em")
    private OffsetDateTime lidaEm;

    @Column(name = "entidade_tipo", length = 40)
    private String entidadeTipo;

    @Column(name = "entidade_id")
    private Long entidadeId;

    protected Notificacao() { }

    public Notificacao(Usuario destinatario, String tipo, String mensagem, OffsetDateTime criadaEm,
                      Boolean lida, String entidadeTipo, Long entidadeId) {
        this.destinatario = destinatario;
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.criadaEm = criadaEm;
        this.lida = lida;
        this.entidadeTipo = entidadeTipo;
        this.entidadeId = entidadeId;
    }

    public void marcarComoLida(OffsetDateTime lidaEm) {
        this.lida = true;
        this.lidaEm = lidaEm;
    }

    public Long getId() { return id; }
    public Usuario getDestinatario() { return destinatario; }
    public String getTipo() { return tipo; }
    public String getMensagem() { return mensagem; }
    public OffsetDateTime getCriadaEm() { return criadaEm; }
    public Boolean getLida() { return lida; }
    public OffsetDateTime getLidaEm() { return lidaEm; }
    public String getEntidadeTipo() { return entidadeTipo; }
    public Long getEntidadeId() { return entidadeId; }
}

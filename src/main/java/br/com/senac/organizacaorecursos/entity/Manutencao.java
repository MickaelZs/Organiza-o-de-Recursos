package br.com.senac.organizacaorecursos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "manutencoes")
public class Manutencao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso", nullable = false, length = 20)
    private TipoRecurso tipoRecurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id")
    private Sala sala;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @NotNull
    @Column(name = "inicio", nullable = false)
    private OffsetDateTime inicio;

    @NotNull
    @Column(name = "termino", nullable = false)
    private OffsetDateTime termino;

    @NotBlank
    @Column(nullable = false)
    private String motivo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    @NotNull
    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusManutencao status;

    protected Manutencao() { }

    public Manutencao(TipoRecurso tipoRecurso, Sala sala, Professor professor, Material material,
                      OffsetDateTime inicio, OffsetDateTime termino, String motivo,
                      Usuario responsavel, OffsetDateTime criadoEm, StatusManutencao status) {
        this.tipoRecurso = tipoRecurso;
        this.sala = sala;
        this.professor = professor;
        this.material = material;
        this.inicio = inicio;
        this.termino = termino;
        this.motivo = motivo;
        this.responsavel = responsavel;
        this.criadoEm = criadoEm;
        this.status = status;
    }

    @AssertTrue(message = "O termino deve ser posterior ao inicio")
    public boolean isIntervaloValido() {
        return inicio == null || termino == null || termino.isAfter(inicio);
    }

    @AssertTrue(message = "A manutencao deve estar associada a exatamente um recurso")
    public boolean isRecursoValido() {
        int recursosAssociados = (sala == null ? 0 : 1)
                + (professor == null ? 0 : 1)
                + (material == null ? 0 : 1);
        return recursosAssociados == 1;
    }

    public void cancelar() {
        if (status != StatusManutencao.ATIVA) {
            throw new IllegalStateException("Manutencao fora de ATIVA");
        }
        status = StatusManutencao.CANCELADA;
    }

    public Long getId() { return id; }
    public TipoRecurso getTipoRecurso() { return tipoRecurso; }
    public Sala getSala() { return sala; }
    public Professor getProfessor() { return professor; }
    public Material getMaterial() { return material; }
    public OffsetDateTime getInicio() { return inicio; }
    public OffsetDateTime getTermino() { return termino; }
    public String getMotivo() { return motivo; }
    public Usuario getResponsavel() { return responsavel; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public StatusManutencao getStatus() { return status; }
}

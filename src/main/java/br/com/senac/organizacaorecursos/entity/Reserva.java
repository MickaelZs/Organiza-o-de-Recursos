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
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reservas")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusReserva status;

    protected Reserva() { }

    public Reserva(Usuario solicitante, Sala sala, Professor professor, Material material,
                   OffsetDateTime inicio, OffsetDateTime termino, StatusReserva status) {
        this.solicitante = solicitante;
        this.sala = sala;
        this.professor = professor;
        this.material = material;
        this.inicio = inicio;
        this.termino = termino;
        this.status = status;
    }

    @AssertTrue(message = "O termino deve ser posterior ao inicio")
    public boolean isIntervaloValido() {
        return inicio == null || termino == null || termino.isAfter(inicio);
    }

    @AssertTrue(message = "A reserva deve estar associada a exatamente um recurso")
    public boolean isRecursoValido() {
        int recursosAssociados = (sala == null ? 0 : 1)
                + (professor == null ? 0 : 1)
                + (material == null ? 0 : 1);
        return recursosAssociados == 1;
    }

    public void atualizar(Sala sala, Professor professor, Material material,
                          OffsetDateTime inicio, OffsetDateTime termino) {
        this.sala = sala;
        this.professor = professor;
        this.material = material;
        this.inicio = inicio;
        this.termino = termino;
    }

    public void cancelar() {
        this.status = StatusReserva.CANCELADA;
    }

    public Long getId() { return id; }
    public Usuario getSolicitante() { return solicitante; }
    public Sala getSala() { return sala; }
    public Professor getProfessor() { return professor; }
    public Material getMaterial() { return material; }
    public OffsetDateTime getInicio() { return inicio; }
    public OffsetDateTime getTermino() { return termino; }
    public StatusReserva getStatus() { return status; }
}

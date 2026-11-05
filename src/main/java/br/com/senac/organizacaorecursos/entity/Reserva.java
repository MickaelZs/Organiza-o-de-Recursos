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

    @Column(name = "retirado_em")
    private OffsetDateTime retiradoEm;

    @Column(name = "devolvido_em")
    private OffsetDateTime devolvidoEm;

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
                          OffsetDateTime inicio, OffsetDateTime termino, StatusReserva status) {
        this.sala = sala;
        this.professor = professor;
        this.material = material;
        this.inicio = inicio;
        this.termino = termino;
        this.status = status;
    }

    public boolean envolveRecursoRestrito() {
        return (sala != null && Boolean.TRUE.equals(sala.getRestrita()))
                || (material != null && Boolean.TRUE.equals(material.getRestrito()));
    }

    public void aprovar() {
        if (status != StatusReserva.SOLICITADA) throw new IllegalStateException("Reserva fora de SOLICITADA");
        status = StatusReserva.APROVADA;
    }

    public void rejeitar() {
        if (status != StatusReserva.SOLICITADA) throw new IllegalStateException("Reserva fora de SOLICITADA");
        status = StatusReserva.REJEITADA;
    }

    public void cancelar() {
        this.status = StatusReserva.CANCELADA;
    }

    public void registrarRetirada(OffsetDateTime retiradoEm) {
        if (status != StatusReserva.APROVADA) throw new IllegalStateException("Reserva fora de APROVADA");
        if (this.retiradoEm != null) throw new IllegalStateException("Retirada ja registrada");
        this.retiradoEm = retiradoEm;
        this.status = StatusReserva.EM_USO;
    }

    public void registrarDevolucao(OffsetDateTime devolvidoEm) {
        if (status != StatusReserva.EM_USO) throw new IllegalStateException("Reserva fora de EM_USO");
        if (this.retiradoEm == null) throw new IllegalStateException("Reserva sem retirada registrada");
        if (this.devolvidoEm != null) throw new IllegalStateException("Devolucao ja registrada");
        this.devolvidoEm = devolvidoEm;
        this.status = StatusReserva.CONCLUIDA;
    }

    public Long getId() { return id; }
    public Usuario getSolicitante() { return solicitante; }
    public Sala getSala() { return sala; }
    public Professor getProfessor() { return professor; }
    public Material getMaterial() { return material; }
    public OffsetDateTime getInicio() { return inicio; }
    public OffsetDateTime getTermino() { return termino; }
    public StatusReserva getStatus() { return status; }
    public OffsetDateTime getRetiradoEm() { return retiradoEm; }
    public OffsetDateTime getDevolvidoEm() { return devolvidoEm; }
}

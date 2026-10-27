package br.com.senac.organizacaorecursos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Column(nullable = false) private String nome;
    @NotBlank @Email @Column(nullable = false, unique = true) private String email;
    @NotBlank @Column(nullable = false) private String senha;
    @NotNull @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Perfil perfil;
    @NotNull @Column(nullable = false) private Boolean ativo;
    @NotNull @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;

    protected Usuario() { }

    public Usuario(String nome, String email, String senha, Perfil perfil, Boolean ativo) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = ativo;
    }

    @PrePersist
    void definirDataDeCriacao() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public Perfil getPerfil() { return perfil; }
    public Boolean getAtivo() { return ativo; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

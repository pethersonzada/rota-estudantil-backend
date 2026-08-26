package com.vanapp.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "presencas")
public class Presenca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "viagem_id", nullable = false)
    private Viagem viagem;

    private LocalDate data;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Viagem getViagem() { return viagem; }
    public void setViagem(Viagem viagem) { this.viagem = viagem; }
    
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getUsuarioId() {
        return this.usuario != null ? this.usuario.getId() : null;
    }

    public Long getViagemId() {
        return this.viagem != null ? this.viagem.getId() : null;
    }
}
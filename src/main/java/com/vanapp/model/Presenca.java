package com.vanapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "presencas")
public class Presenca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private LocalDate data;
    private String status;

    // Métodos essenciais
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // O "atalho" para o erro do seu Controller parar de reclamar
    public Long getUsuarioId() {
        return this.usuario != null ? this.usuario.getId() : null;
    }
}
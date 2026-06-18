package com.vanapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vanapp.model.Usuario;
import com.vanapp.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final GeocodingService geocodingService;

    public UsuarioService(UsuarioRepository usuarioRepository, GeocodingService geocodingService) {
        this.usuarioRepository = usuarioRepository;
        this.geocodingService = geocodingService;
    }

    public Usuario cadastrarUsuario(Usuario usuario) {
        if (usuarioRepository.findByCpf(usuario.getCpf()).isPresent()) {
            throw new RuntimeException("CPF já cadastrado");
        }
        if (usuarioRepository.findByTelefone(usuario.getTelefone()).isPresent()) {
            throw new RuntimeException("Telefone já cadastrado");
        }
        
        double[] coordenadas = geocodingService.geocodificarEndereco(usuario.getEnderecoCompleto());
        usuario.setLatitude(coordenadas[0]);
        usuario.setLongitude(coordenadas[1]);
        
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorId(Long id) {
        if (id == null) throw new RuntimeException("id não pode ser nulo");
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public List<Usuario> listarPassageiros() {
        return usuarioRepository.findAll()
                .stream()
                .filter(u -> "PASSAGEIRO".equals(u.getTipo()))
                .toList();
    }
}
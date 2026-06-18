package com.vanapp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vanapp.model.Usuario;
import com.vanapp.repository.PresencaRepository;
import com.vanapp.repository.UsuarioRepository;
import com.vanapp.repository.ViagemRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final GeocodingService geocodingService;
    private final PresencaRepository presencaRepository;
    private final ViagemRepository viagemRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, GeocodingService geocodingService, PresencaRepository presencaRepository, ViagemRepository viagemRepository) {
        this.usuarioRepository = usuarioRepository;
        this.geocodingService = geocodingService;
        this.presencaRepository = presencaRepository;
        this.viagemRepository = viagemRepository;
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

    @Transactional
    public void excluirUsuario(Long id) {
        if (id == null) throw new RuntimeException("O ID fornecido para exclusão não pode ser nulo.");

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado no sistema."));
        
        if ("MOTORISTA".equals(usuario.getTipo())) {
            if (viagemRepository.existsByMotoristaIdAndStatus(id, "EM_ANDAMENTO")) {
                throw new RuntimeException("Não é possível excluir a conta com uma rota em andamento. Encerre a viagem primeiro.");
            }
            viagemRepository.deleteAllByMotoristaId(id);
        }

        presencaRepository.deleteAllByUsuarioId(id); 
        usuarioRepository.deleteById(id);
    }
}
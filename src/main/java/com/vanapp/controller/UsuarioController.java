package com.vanapp.controller;

import com.vanapp.model.Usuario;
import com.vanapp.model.Presenca;
import com.vanapp.repository.UsuarioRepository;
import com.vanapp.repository.PresencaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PresencaRepository presencaRepository;

    public static class PassageiroDTO {
        public Long id;
        public String nome;
        public String status;
        public Double latitude; // Adicionado para garantir que o mapa receba
        public Double longitude;

        public PassageiroDTO(Usuario u, String status) {
            this.id = u.getId();
            this.nome = u.getNome();
            this.status = status;
            this.latitude = u.getLatitude();
            this.longitude = u.getLongitude();
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/passageiros")
    public ResponseEntity<List<PassageiroDTO>> listarPassageiros() {
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Recife"));
        List<Usuario> passageiros = usuarioRepository.findByTipo("PASSAGEIRO");
        return ResponseEntity.ok(passageiros.stream().map(u -> {
            Presenca p = presencaRepository.findByUsuarioIdAndData(u.getId(), hoje);
            return new PassageiroDTO(u, (p != null) ? p.getStatus() : null);
        }).collect(Collectors.toList()));
    }

    // NOVO ENDPOINT ADICIONADO PARA O MAPA FUNCIONAR
    @GetMapping("/motorista")
    public ResponseEntity<Usuario> getMotorista() {
        return usuarioRepository.findByTipo("MOTORISTA")
                .stream()
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).build());
    }

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Usuario loginRequest) {
        return usuarioRepository.findByCpf(loginRequest.getCpf().trim())
                .filter(u -> u.getSenha().trim().equals(loginRequest.getSenha().trim()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@RequestBody Usuario novoUsuario) {
        if (usuarioRepository.findByCpf(novoUsuario.getCpf()).isPresent()) {
            return ResponseEntity.status(400).body("Erro");
        }
        return ResponseEntity.ok(usuarioRepository.save(novoUsuario));
    }
}
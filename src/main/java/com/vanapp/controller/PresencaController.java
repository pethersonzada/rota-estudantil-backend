package com.vanapp.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vanapp.model.Presenca;
import com.vanapp.model.Usuario;
import com.vanapp.repository.PresencaRepository;
import com.vanapp.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "3. Gestão de Presenças", description = "Operações para controle de embarque diário")
@RestController
@RequestMapping("/presenca")
@CrossOrigin(origins = "*")
public class PresencaController {

    private final PresencaRepository presencaRepository;
    private final UsuarioRepository usuarioRepository;

    public PresencaController(PresencaRepository presencaRepository, UsuarioRepository usuarioRepository) {
        this.presencaRepository = presencaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Marcar Presença", description = "Registra o status do passageiro para a data atual.")
    @PostMapping("/marcar")
    public ResponseEntity<?> marcarPresenca(@RequestBody Map<String, Object> payload) {
        Object rawId = payload.get("usuarioId");
        if (rawId == null) {
            rawId = payload.get("passageiroId");
        }

        if (rawId == null) {
            return ResponseEntity.status(400).body("Erro: usuarioId é obrigatório");
        }

        Long usuarioId = Long.valueOf(rawId.toString());
        String status = (String) payload.get("status");

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(404).body("Erro: Usuário não encontrado");
        }

        LocalDate hoje = LocalDate.now(ZoneId.of("America/Recife"));
        Presenca existente = presencaRepository.findByUsuarioIdAndData(usuarioId, hoje);

        if (existente != null) {
            existente.setStatus(status);
            existente.setViagem(null);
            presencaRepository.save(existente);
        } else {
            Presenca novaPresenca = new Presenca();
            novaPresenca.setUsuario(usuario);
            novaPresenca.setData(hoje);
            novaPresenca.setStatus(status);
            novaPresenca.setViagem(null);
            presencaRepository.save(novaPresenca);
        }

        return ResponseEntity.ok("Presença registrada com sucesso");
    }
}
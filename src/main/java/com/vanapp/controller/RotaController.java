package com.vanapp.controller;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vanapp.model.Presenca;
import com.vanapp.model.Usuario;
import com.vanapp.repository.PresencaRepository;
import com.vanapp.repository.UsuarioRepository;
import com.vanapp.service.RotaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "2. Gestão de Rotas", description = "Operações de presença e otimização logística")
@RestController
@RequestMapping("/rota")
@CrossOrigin(origins = "*")
public class RotaController {
    @Autowired private PresencaRepository presencaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RotaService rotaService;

    @Operation(summary = "Confirmar/Atualizar Presença", description = "Marca a presença do passageiro para o dia atual ou limpa o registro.")
    @PostMapping("/confirmar")
    @Transactional
    public ResponseEntity<String> confirmarPresenca(@RequestParam Long usuarioId, @RequestParam String status) {
        if (usuarioId == null) {
            return ResponseEntity.badRequest().body("usuarioId não pode ser nulo");
        }

        LocalDate hoje = LocalDate.now(ZoneId.of("America/Recife"));
        Presenca p = presencaRepository.findByUsuarioIdAndData(usuarioId, hoje);

        if ("LIMPAR".equals(status)) {
            if (p != null) {
                presencaRepository.delete(p);
            }
        } else {
            if (p == null) {
                p = new Presenca();
                Usuario user = usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
                p.setUsuario(user);
                p.setData(hoje);
            }
            p.setStatus(status);
            presencaRepository.save(p);
        }
        return ResponseEntity.ok("SUCESSO");
    }

    @Operation(summary = "Otimizar Rota", description = "Calcula a melhor sequência de paradas para o motorista com base na localização dos passageiros.")
    @GetMapping("/otimizar/{motoristaId}")
    public ResponseEntity<?> otimizarRota(@PathVariable Long motoristaId, @RequestParam String sentido) {
        if (motoristaId == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "motoristaId não pode ser nulo"));
        }
        try {
            List<Usuario> rotaOtimizada = rotaService.otimizarRota(motoristaId, sentido);
            return ResponseEntity.ok(rotaOtimizada);
        } catch (Exception e) {
            Map<String, String> erroJson = new HashMap<>();
            erroJson.put("erro", e.getMessage());
            return ResponseEntity.status(500).body(erroJson);
        }
    }
}
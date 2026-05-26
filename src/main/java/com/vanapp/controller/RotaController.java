package com.vanapp.controller;

import com.vanapp.model.Usuario;
import com.vanapp.model.Presenca;
import com.vanapp.repository.UsuarioRepository;
import com.vanapp.repository.PresencaRepository;
import com.vanapp.service.RotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rota")
@CrossOrigin(origins = "*")
public class RotaController {

    @Autowired private PresencaRepository presencaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RotaService rotaService;

    @PostMapping("/confirmar")
    @Transactional
    public ResponseEntity<String> confirmarPresenca(@RequestParam Long usuarioId, @RequestParam String status) {
        System.out.println("DEBUG RECEBIDO: ID " + usuarioId + " | STATUS: " + status);
        
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Recife"));
        Presenca p = presencaRepository.findByUsuarioIdAndData(usuarioId, hoje);
        
        if ("LIMPAR".equals(status)) {
            if (p != null) {
                presencaRepository.delete(p);
                System.out.println("DEBUG: DELETADO");
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
            System.out.println("DEBUG: SALVO COM STATUS: " + status);
        }
        return ResponseEntity.ok("SUCESSO");
    }

    @GetMapping("/otimizar/{motoristaId}")
    public ResponseEntity<?> otimizarRota(@PathVariable Long motoristaId, @RequestParam String sentido) {
        try {
            System.out.println("DEBUG: Chamando otimização para motorista " + motoristaId + " | Sentido: " + sentido);
            List<Usuario> rotaOtimizada = rotaService.otimizarRota(motoristaId, sentido);
            return ResponseEntity.ok(rotaOtimizada);
        } catch (Exception e) {
            System.err.println("DEBUG: ERRO NA OTIMIZAÇÃO: " + e.getMessage());
            Map<String, String> erroJson = new HashMap<>();
            erroJson.put("erro", e.getMessage());
            return ResponseEntity.status(500).body(erroJson);
        }
    }
} // <--- ESTA CHAVE FECHA A CLASSE
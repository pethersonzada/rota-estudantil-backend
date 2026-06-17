package com.vanapp.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vanapp.model.Presenca;
import com.vanapp.model.Usuario;
import com.vanapp.repository.PresencaRepository;
import com.vanapp.repository.UsuarioRepository;
import com.vanapp.service.RotaService;
import com.vanapp.service.ViagemService;

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
    @Autowired private ViagemService viagemService;

    // Estado em memória (mantido para alta performance do GPS)
    private static final Map<String, Double> localizacaoAtualVan = new ConcurrentHashMap<>();
    private static boolean rotaAtiva = false;

    // -------------------------------------------------------------------
    // NOVOS ENDPOINTS: INTEGRAÇÃO DO CICLO DE VIDA DA VIAGEM
    // -------------------------------------------------------------------

    @Operation(summary = "Iniciar Rota", description = "O motorista inicia a viagem oficialmente.")
    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciarRota(@RequestBody Map<String, Object> payload) {
        try {
            Long motoristaId = Long.valueOf(payload.get("motoristaId").toString());
            String sentido = payload.get("sentido").toString();
            
            // Salva no banco
            viagemService.iniciarRota(motoristaId, sentido);
            
            // Ativa na memória para o GPS
            rotaAtiva = true;
            localizacaoAtualVan.clear();
            
            return ResponseEntity.ok("Rota iniciada com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @Operation(summary = "Encerrar Rota", description = "O motorista finaliza a viagem oficialmente.")
    @PostMapping("/encerrar")
    public ResponseEntity<?> encerrarRota() {
        // Finaliza no banco
        viagemService.encerrarRota();
        
        // Desativa na memória
        rotaAtiva = false;
        localizacaoAtualVan.clear();
        
        return ResponseEntity.ok("Rota encerrada com sucesso.");
    }

    @Operation(summary = "Status Atual", description = "Verifica se há viagem em andamento.")
    @GetMapping("/status-atual")
    public ResponseEntity<Map<String, String>> getStatusAtual() {
        return ResponseEntity.ok(viagemService.verificarStatusAtual());
    }

    // -------------------------------------------------------------------
    // ENDPOINTS ORIGINAIS MANTIDOS
    // -------------------------------------------------------------------

    @Operation(summary = "Confirmar/Atualizar Presença", description = "Marca a presença do passageiro para o dia atual ou limpa o registro.")
    @PostMapping("/confirmar")
    @Transactional
    public ResponseEntity<String> confirmarPresenca(@RequestParam Long usuarioId, @RequestParam String status) {
        if (usuarioId == null) return ResponseEntity.badRequest().body("usuarioId não pode ser nulo");

        LocalDate hoje = LocalDate.now(ZoneId.of("America/Recife"));
        Presenca p = presencaRepository.findByUsuarioIdAndData(usuarioId, hoje);

        if ("LIMPAR".equals(status)) {
            if (p != null) presencaRepository.delete(p);
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

    @Operation(summary = "Otimizar Rota", description = "Calcula a melhor sequência de paradas.")
    @GetMapping("/otimizar/{motoristaId}")
    public ResponseEntity<?> otimizarRota(@PathVariable Long motoristaId, @RequestParam String sentido) {
        try {
            List<Usuario> rotaOtimizada = rotaService.otimizarRota(motoristaId, sentido);
            return ResponseEntity.ok(rotaOtimizada);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", e.getMessage()));
        }
    }

    @Operation(summary = "Atualizar Localização", description = "Motorista transmite coordenadas reais.")
    @PostMapping("/localizacao-van")
    public ResponseEntity<String> atualizarLocalizacaoVan(@RequestParam Double latitude, @RequestParam Double longitude) {
        if (!rotaAtiva) return ResponseEntity.badRequest().body("Rota inativa.");
        
        localizacaoAtualVan.put("latitude", latitude);
        localizacaoAtualVan.put("longitude", longitude);
        return ResponseEntity.ok("Localização atualizada.");
    }

    @Operation(summary = "Buscar Localização", description = "Passageiro consulta radar. Retorna 404 se a rota estiver inativa.")
    @GetMapping("/localizacao-van")
    public ResponseEntity<Map<String, Double>> obterLocalizacaoVan() {
        if (!rotaAtiva || localizacaoAtualVan.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(localizacaoAtualVan);
    }
}
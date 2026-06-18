package com.vanapp.controller;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vanapp.model.Presenca;
import com.vanapp.model.Usuario;
import com.vanapp.repository.PresencaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "3. Gestão de Presenças", description = "Operações para controle de embarque diário")
@RestController
@RequestMapping("/presenca")
@CrossOrigin(origins = "*")
public class PresencaController {

    private final PresencaRepository presencaRepository;

    public PresencaController(PresencaRepository presencaRepository) {
        this.presencaRepository = presencaRepository;
    }

    @Operation(summary = "Marcar Presença", description = "Registra o status do passageiro (ex: embarcado, ausente) para a data atual.")
    @PostMapping("/marcar")
    public ResponseEntity<?> marcarPresenca(@RequestBody Presenca presenca) {
        if (presenca.getUsuarioId() == null) {
            return ResponseEntity.status(400).body("Erro: usuarioId é obrigatório");
        }

        LocalDate hoje = LocalDate.now(ZoneId.of("America/Recife"));
        Presenca existente = presencaRepository.findByUsuarioIdAndData(presenca.getUsuarioId(), hoje);

        if (existente != null) {
            existente.setStatus(presenca.getStatus());
            presencaRepository.save(existente);
        } else {
            Usuario user = new Usuario();
            user.setId(presenca.getUsuarioId());
            presenca.setUsuario(user);
            presenca.setData(hoje);
            presencaRepository.save(presenca);
        }
        return ResponseEntity.ok("Presença registrada com sucesso");
    }
}
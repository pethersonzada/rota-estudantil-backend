package com.vanapp.controller;

import com.vanapp.model.Presenca;
import com.vanapp.model.Usuario;
import com.vanapp.repository.PresencaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/presenca")
@CrossOrigin(origins = "*")
public class PresencaController {

    @Autowired private PresencaRepository presencaRepository;

    @PostMapping("/marcar")
    public ResponseEntity<?> marcarPresenca(@RequestBody Presenca presenca) {
        if (presenca.getUsuarioId() == null) {
            return ResponseEntity.status(400).body("usuarioId nulo");
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
        return ResponseEntity.ok("Registrado");
    }
}
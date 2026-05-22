package com.vanapp.controller;

import com.vanapp.model.Usuario;
import com.vanapp.service.RotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rota")
@CrossOrigin(origins = "*")
public class RotaController {

    @Autowired
    private RotaService rotaService;

    // A porta agora está preparada para escutar qual é o sentido da viagem
    @GetMapping("/otimizar/{motoristaId}")
    public ResponseEntity<?> otimizarRota(
            @PathVariable Long motoristaId,
            @RequestParam(value = "sentido", defaultValue = "ida") String sentido) {
        try {
            System.out.println("--- OTIMIZANDO ROTA DE " + sentido.toUpperCase() + " ---");
            // Entregamos o ID e o Sentido para o motor de busca (Service)
            List<Usuario> rota = rotaService.otimizarRota(motoristaId, sentido);
            return ResponseEntity.ok(rota);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
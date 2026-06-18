package com.vanapp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vanapp.model.Usuario;
import com.vanapp.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "4. Autenticação", description = "Segurança e acesso ao sistema")
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Login do Usuário", description = "Verifica CPF e Senha e retorna o ID e o Tipo de perfil do usuário.")
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> credenciais) {
        String cpf = credenciais.get("cpf");
        String senha = credenciais.get("senha");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(cpf);

        if (usuarioOpt.isPresent() && usuarioOpt.get().getSenha().equals(senha)) {
            Usuario u = usuarioOpt.get();
            Map<String, String> resposta = new HashMap<>();
            resposta.put("id", u.getId().toString());
            resposta.put("tipo", u.getTipo());
            resposta.put("nome", u.getNome() != null ? u.getNome() : "");
            resposta.put("endereco", u.getEnderecoCompleto() != null ? u.getEnderecoCompleto() : "");
            return ResponseEntity.ok(resposta);
        } else {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
    }
}
package com.vanapp.controller;

import com.vanapp.dto.EnderecoRequest;
import com.vanapp.model.Usuario;
import com.vanapp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // --- 1. A VITRINE DOS PASSAGEIROS ---
    @GetMapping("/passageiros")
    public ResponseEntity<List<Usuario>> listarPassageiros() {
        System.out.println("--- LISTANDO PASSAGEIROS PARA A CHAMADA ---");
        return ResponseEntity.ok(usuarioRepository.findByTipo("PASSAGEIRO"));
    }

    // --- 2. A BUSCA DINÂMICA PELO PILOTO DA VAN ---
    @GetMapping("/motorista")
    public ResponseEntity<Usuario> buscarMotoristaDaVan() {
        System.out.println("--- PROCURANDO O MOTORISTA NO BANCO ---");
        List<Usuario> motoristas = usuarioRepository.findByTipo("MOTORISTA");
        
        if (!motoristas.isEmpty()) {
            return ResponseEntity.ok(motoristas.get(0));
        }
        
        System.out.println("Erro: Nenhum usuário com tipo MOTORISTA encontrado.");
        return ResponseEntity.notFound().build();
    }

    // --- 3. BUSCA UM USUÁRIO QUALQUER PELO ID ---
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            return ResponseEntity.ok(usuario.get());
        }
        return ResponseEntity.notFound().build();
    }

    // --- 4. CADASTRO ---
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@RequestBody Usuario novoUsuario) {
        System.out.println("--- TENTATIVA DE CADASTRO ---");
        try {
            Optional<Usuario> usuarioExistente = usuarioRepository.findByCpf(novoUsuario.getCpf());
            if (usuarioExistente.isPresent()) {
                return ResponseEntity.status(400).body("Este CPF já está cadastrado.");
            }
            Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
            return ResponseEntity.ok(usuarioSalvo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno ao tentar salvar o cadastro.");
        }
    }

    // --- 5. LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Usuario loginRequest) {
        String cpfLimpo = loginRequest.getCpf().trim();
        String senhaLimpa = loginRequest.getSenha().trim();

        System.out.println("--- TENTATIVA DE LOGIN: " + cpfLimpo + " ---");
        Optional<Usuario> usuario = usuarioRepository.findByCpf(cpfLimpo);
        
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            if (u.getSenha().trim().equals(senhaLimpa)) {
                return ResponseEntity.ok(u);
            }
        }
        return ResponseEntity.status(401).build();
    }

    // --- 6. SALVAR ENDEREÇO DO MAPA ---
    @PutMapping("/salvar-endereco")
    public ResponseEntity<String> salvarEndereco(@RequestBody EnderecoRequest request) {
        System.out.println("--- SALVANDO ENDEREÇO ---");
        if (request.getIdUsuario() == null) {
            return ResponseEntity.badRequest().body("ID do usuário é obrigatório.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(request.getIdUsuario());
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setLatitude(request.getLatitude());
            usuario.setLongitude(request.getLongitude());
            usuario.setEnderecoCompleto(request.getEnderecoCompleto());
            
            usuarioRepository.save(usuario);
            return ResponseEntity.ok("Endereço atualizado com sucesso!");
        }
        return ResponseEntity.status(404).body("Usuário não encontrado.");
    }
}
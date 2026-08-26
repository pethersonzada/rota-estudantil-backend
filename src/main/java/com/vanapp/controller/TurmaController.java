package com.vanapp.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vanapp.model.Presenca;
import com.vanapp.model.Turma;
import com.vanapp.model.Usuario;
import com.vanapp.repository.PresencaRepository;
import com.vanapp.service.TurmaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "4. Gestão de Turmas", description = "Endpoints para gerenciamento de rotas e turnos")
@RestController
@RequestMapping("/turmas")
@CrossOrigin(origins = "*")
public class TurmaController {

    private final TurmaService turmaService;
    private final PresencaRepository presencaRepository;

    public TurmaController(TurmaService turmaService, PresencaRepository presencaRepository) {
        this.turmaService = turmaService;
        this.presencaRepository = presencaRepository;
    }

    @Operation(summary = "Criar Turma", description = "Cadastra uma nova rota/turno para o motorista.")
    @PostMapping
    public ResponseEntity<?> criarTurma(@RequestBody Turma turma) {
        try {
            Turma novaTurma = turmaService.criarTurma(turma);
            return ResponseEntity.ok(novaTurma);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Listar Turmas do Motorista", description = "Retorna todas as turmas vinculadas a um motorista específico.")
    @GetMapping("/motorista/{motoristaId}")
    public ResponseEntity<List<Turma>> listarPorMotorista(@PathVariable Long motoristaId) {
        List<Turma> turmas = turmaService.listarTurmasPorMotorista(motoristaId);
        return ResponseEntity.ok(turmas);
    }

    @Operation(summary = "Listar Passageiros da Turma", description = "Retorna os passageiros vinculados a uma turma junto com o status de presença do dia.")
    @GetMapping("/{turmaId}/passageiros")
    public ResponseEntity<List<Map<String, Object>>> listarPassageirosDaTurma(@PathVariable Long turmaId) {
        List<Usuario> passageiros = turmaService.listarPassageirosPorTurma(turmaId);
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Recife"));

        List<Map<String, Object>> resultado = passageiros.stream().map(aluno -> {
            Presenca presenca = presencaRepository.findByUsuarioIdAndData(aluno.getId(), hoje);
            
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", aluno.getId());
            map.put("nome", aluno.getNome());
            map.put("telefone", aluno.getTelefone());
            map.put("enderecoCompleto", aluno.getEnderecoCompleto());
            map.put("status", presenca != null ? presenca.getStatus() : null);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Adicionar Aluno à Turma", description = "Vincula um usuário/passageiro a uma turma específica.")
    @PostMapping("/{turmaId}/alunos/{alunoId}")
    public ResponseEntity<?> adicionarAlunoTurma(@PathVariable Long turmaId, @PathVariable Long alunoId) {
        try {
            turmaService.adicionarAluno(turmaId, alunoId);
            return ResponseEntity.ok("Aluno vinculado com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Buscar Turma do Aluno", description = "Retorna a turma vinculada a um usuário/aluno específico.")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> buscarTurmaPorUsuario(@PathVariable Long usuarioId) {
        try {
            Turma turma = turmaService.buscarTurmaPorAlunoId(usuarioId);
            if (turma == null) {
                return ResponseEntity.status(404).body("Aluno não vinculado a nenhuma turma.");
            }
            return ResponseEntity.ok(turma);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Atualizar Turma", description = "Altera o nome ou turno de uma turma existente.")
    @PutMapping("/{turmaId}")
    public ResponseEntity<?> atualizarTurma(@PathVariable Long turmaId, @RequestBody Turma turmaAtualizada) {
        try {
            Turma turmaModificada = turmaService.atualizarTurma(turmaId, turmaAtualizada);
            return ResponseEntity.ok(turmaModificada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
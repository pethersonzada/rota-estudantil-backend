package com.vanapp.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.vanapp.model.Turma;
import com.vanapp.model.Usuario;
import com.vanapp.repository.TurmaRepository;
import com.vanapp.repository.UsuarioRepository;

@Service
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final UsuarioRepository usuarioRepository;

    public TurmaService(TurmaRepository turmaRepository, UsuarioRepository usuarioRepository) {
        this.turmaRepository = turmaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Turma criarTurma(Turma turma) {
        if (turma.getMotorista() == null || turma.getMotorista().getId() == null) {
            throw new RuntimeException("O ID do motorista é obrigatório para criar uma turma.");
        }
        
        Usuario motorista = usuarioRepository.findById(turma.getMotorista().getId())
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado."));
        
        turma.setMotorista(motorista);
        return turmaRepository.save(turma);
    }

    public List<Turma> listarTurmasPorMotorista(Long motoristaId) {
        return turmaRepository.findByMotoristaId(motoristaId);
    }

    public List<Usuario> listarPassageirosPorTurma(Long turmaId) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada."));
        return List.copyOf(turma.getAlunos());
    }

    public void adicionarAluno(Long turmaId, Long alunoId) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada."));
        
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado."));
        
        turma.getAlunos().add(aluno);
        turmaRepository.save(turma);
    }

    public Turma buscarTurmaPorAlunoId(Long alunoId) {
        return turmaRepository.findAll().stream()
                .filter(turma -> turma.getAlunos().stream().anyMatch(aluno -> aluno.getId().equals(alunoId)))
                .findFirst()
                .orElse(null);
    }

    public Turma atualizarTurma(Long turmaId, Turma turmaAtualizada) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada."));
        
        turma.setNome(turmaAtualizada.getNome());
        turma.setTurno(turmaAtualizada.getTurno());
        
        return turmaRepository.save(turma);
    }
}
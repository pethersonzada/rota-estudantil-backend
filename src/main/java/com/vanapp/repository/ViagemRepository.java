package com.vanapp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vanapp.model.Viagem;

public interface ViagemRepository extends JpaRepository<Viagem, Long> {
    Optional<Viagem> findFirstByStatus(String status);
    
    boolean existsByTurmaIdAndStatus(Long turmaId, String status);
    
    boolean existsByTurmaMotoristaIdAndStatus(Long motoristaId, String status);
    
    void deleteAllByTurmaId(Long turmaId);
    
    void deleteAllByTurmaMotoristaId(Long motoristaId);
}
package com.vanapp.repository;

import com.vanapp.model.Presenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface PresencaRepository extends JpaRepository<Presenca, Long> {
    
    @Query("SELECT p FROM Presenca p JOIN FETCH p.usuario WHERE p.data = :data")
    List<Presenca> findByData(@Param("data") LocalDate data);
}
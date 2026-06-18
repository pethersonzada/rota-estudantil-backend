package com.vanapp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vanapp.model.Presenca;

public interface PresencaRepository extends JpaRepository<Presenca, Long> {

    @Query("SELECT p FROM Presenca p JOIN FETCH p.usuario WHERE p.data = :data")
    List<Presenca> findByData(@Param("data") LocalDate data);

    @Query(value = "SELECT * FROM presencas WHERE usuario_id = :usuarioId AND data = CAST(:data AS DATE)", nativeQuery = true)
    Presenca findByUsuarioIdAndData(@Param("usuarioId") Long usuarioId, @Param("data") LocalDate data);

    @Modifying
    @Query("DELETE FROM Presenca p WHERE p.usuario.id = :usuarioId")
    void deleteAllByUsuarioId(@Param("usuarioId") Long usuarioId);
}
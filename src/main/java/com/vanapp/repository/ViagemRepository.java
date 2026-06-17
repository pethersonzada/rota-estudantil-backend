package com.vanapp.repository;

import com.vanapp.model.Viagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViagemRepository extends JpaRepository<Viagem, Long> {
    Optional<Viagem> findFirstByStatus(String status);
}
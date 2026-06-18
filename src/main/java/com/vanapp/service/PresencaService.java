package com.vanapp.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.vanapp.model.Presenca;
import com.vanapp.repository.PresencaRepository;

@Service
public class PresencaService {

    private final PresencaRepository presencaRepository;

    public PresencaService(PresencaRepository presencaRepository) {
        this.presencaRepository = presencaRepository;
    }

    public void registrarPresenca(Presenca presenca) {
        presenca.setData(LocalDate.now());
        presencaRepository.save(presenca);
    }
}
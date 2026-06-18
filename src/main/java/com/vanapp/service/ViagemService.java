package com.vanapp.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vanapp.model.Viagem;
import com.vanapp.repository.ViagemRepository;

@Service
public class ViagemService {

    private final ViagemRepository viagemRepository;

    public ViagemService(ViagemRepository viagemRepository) {
        this.viagemRepository = viagemRepository;
    }

    public void iniciarRota(Long motoristaId, String sentido) {
        Optional<Viagem> viagemAtiva = viagemRepository.findFirstByStatus("EM_ANDAMENTO");
        
        if (viagemAtiva.isPresent()) {
            throw new RuntimeException("Já existe uma rota em andamento.");
        }

        Viagem novaViagem = new Viagem();
        novaViagem.setMotoristaId(motoristaId);
        novaViagem.setSentido(sentido);
        novaViagem.setStatus("EM_ANDAMENTO");

        viagemRepository.save(novaViagem);
    }

    public void encerrarRota() {
        Optional<Viagem> viagemAtiva = viagemRepository.findFirstByStatus("EM_ANDAMENTO");
        
        if (viagemAtiva.isPresent()) {
            Viagem viagem = viagemAtiva.get();
            viagem.setStatus("FINALIZADA");
            viagemRepository.save(viagem);
        }
    }

    public Map<String, String> verificarStatusAtual() {
        Optional<Viagem> viagemAtiva = viagemRepository.findFirstByStatus("EM_ANDAMENTO");
        Map<String, String> resposta = new HashMap<>();

        if (viagemAtiva.isPresent()) {
            resposta.put("status", "ATIVA");
            resposta.put("sentido", viagemAtiva.get().getSentido());
        } else {
            resposta.put("status", "INATIVA");
        }
        return resposta;
    }
}
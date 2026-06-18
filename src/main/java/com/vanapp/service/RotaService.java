package com.vanapp.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.main;
import com.vanapp.model.Presenca;
import com.vanapp.model.Usuario;
import com.vanapp.repository.PresencaRepository;
import com.vanapp.repository.UsuarioRepository;

@Service
public class RotaService {

    private final UsuarioRepository usuarioRepository;
    private final PresencaRepository presencaRepository;

    static { Loader.loadNativeLibraries(); }

    public RotaService(UsuarioRepository usuarioRepository, PresencaRepository presencaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.presencaRepository = presencaRepository;
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    public List<Usuario> otimizarRota(String sentido) {
        // DIAGNÓSTICO: Se travar aqui, você verá no terminal do VS Code
        System.out.println("DEBUG: Iniciando otimização para sentido: " + sentido);
        
        List<Usuario> motoristas = usuarioRepository.findByTipo("MOTORISTA");
        if (motoristas.isEmpty()) {
            System.err.println("ERRO: Nenhum motorista encontrado com tipo 'MOTORISTA' no banco!");
            throw new RuntimeException("Nenhum motorista cadastrado no sistema.");
        }
        
        Usuario motorista = motoristas.get(0);
        System.out.println("DEBUG: Motorista encontrado: " + motorista.getNome());

        List<Presenca> presencasDoDia = presencaRepository.findByData(LocalDate.now());
        List<Usuario> passageiros = presencasDoDia.stream()
                .filter(p -> p.getUsuario() != null && p.getStatus() != null)
                .filter(p -> "AMBOS".equalsIgnoreCase(p.getStatus()) || p.getStatus().equalsIgnoreCase(sentido))
                .map(Presenca::getUsuario)
                .distinct()
                .collect(Collectors.toList());

        if (passageiros.isEmpty()) throw new RuntimeException("Nenhum passageiro confirmado para " + sentido);

        int n = passageiros.size() + 1;
        double[] lats = new double[n];
        double[] lons = new double[n];
        lats[0] = motorista.getLatitude(); lons[0] = motorista.getLongitude();
        for (int i = 0; i < passageiros.size(); i++) {
            lats[i + 1] = passageiros.get(i).getLatitude();
            lons[i + 1] = passageiros.get(i).getLongitude();
        }

        long[][] distancias = new long[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distancias[i][j] = (long) calcularDistancia(lats[i], lons[i], lats[j], lons[j]);
            }
        }

        RoutingIndexManager manager = new RoutingIndexManager(n, 1, 0);
        RoutingModel routing = new RoutingModel(manager);
        routing.setArcCostEvaluatorOfAllVehicles(routing.registerTransitCallback(
                (f, t) -> distancias[manager.indexToNode(f)][manager.indexToNode(t)]));

        Assignment solution = routing.solveWithParameters(
                main.defaultRoutingSearchParameters().toBuilder()
                        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                        .build());

        if (solution == null) throw new RuntimeException("Falha na otimização da rota");

        List<Usuario> rotaOtimizada = new ArrayList<>();
        long index = routing.start(0);
        while ((index = solution.value(routing.nextVar(index))) != routing.end(0)) {
            rotaOtimizada.add(passageiros.get(manager.indexToNode(index) - 1));
        }

        if ("volta".equalsIgnoreCase(sentido)) Collections.reverse(rotaOtimizada);
        return rotaOtimizada;
    }
}
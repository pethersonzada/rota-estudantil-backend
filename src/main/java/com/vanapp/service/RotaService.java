package com.vanapp.service;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import com.google.protobuf.Duration;
import com.vanapp.model.Usuario;
import com.vanapp.model.Presenca;
import com.vanapp.repository.UsuarioRepository;
import com.vanapp.repository.PresencaRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RotaService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PresencaRepository presencaRepository;

    static { Loader.loadNativeLibraries(); }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<Usuario> otimizarRota(Long motoristaId, String sentido) {
        // 1. FILTRAGEM INTELIGENTE: Remove duplicatas e pega apenas o último status do dia
        List<Presenca> presencasDoDia = presencaRepository.findByData(LocalDate.now());
        
        List<Usuario> passageiros = presencasDoDia.stream()
                .collect(Collectors.toMap(
                        p -> p.getUsuario().getId(), 
                        p -> p, 
                        (p1, p2) -> p2 // Em caso de duplicata, mantém o último registro
                ))
                .values()
                .stream()
                .filter(p -> {
                    String status = p.getStatus();
                    return "ida".equalsIgnoreCase(sentido) 
                        ? ("IDA_E_VOLTA".equals(status) || "SO_IDA".equals(status))
                        : ("IDA_E_VOLTA".equals(status) || "SO_VOLTA".equals(status));
                })
                .map(Presenca::getUsuario)
                .distinct()
                .collect(Collectors.toList());

        if (passageiros.isEmpty()) throw new RuntimeException("Nenhum passageiro válido para " + sentido + "!");

        Usuario motorista = usuarioRepository.findById(motoristaId)
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado"));

        // 2. MONTAGEM DA MATRIZ
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

        // 3. ENGINE DE OTIMIZAÇÃO (OR-TOOLS)
        RoutingIndexManager manager = new RoutingIndexManager(n, 1, 0);
        RoutingModel routing = new RoutingModel(manager);
        routing.setArcCostEvaluatorOfAllVehicles(routing.registerTransitCallback((f, t) -> distancias[manager.indexToNode(f)][manager.indexToNode(t)]));
        
        Assignment solution = routing.solveWithParameters(main.defaultRoutingSearchParameters().toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC).build());
        
        if (solution == null) throw new RuntimeException("Falha na otimização");

        List<Usuario> rotaOtimizada = new ArrayList<>();
        long index = routing.start(0);
        index = solution.value(routing.nextVar(index));
        while (!routing.isEnd(index)) {
            rotaOtimizada.add(passageiros.get(manager.indexToNode(index) - 1));
            index = solution.value(routing.nextVar(index));
        }

        // 4. INVERSÃO LÓGICA (Garante que a volta venha invertida)
        if ("volta".equalsIgnoreCase(sentido)) {
            Collections.reverse(rotaOtimizada);
        }

        return rotaOtimizada;
    }
}
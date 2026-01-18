package com.bancario.compensacion.service;

import com.bancario.compensacion.model.CicloCompensacion;
import com.bancario.compensacion.repository.CicloCompensacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class CicloScheduler {

    @Autowired
    private CompensacionService compensacionService;

    @Autowired
    private CicloCompensacionRepository cicloRepo;

    private static final int DURACION_CICLO_MINUTOS = 5; 

    @Scheduled(fixedRate = 30000) 
    public void verificarCierreAutomatico() {
        CicloCompensacion actual = cicloRepo.findAll().stream()
                .filter(c -> "ABIERTO".equals(c.getEstado()))
                .findFirst()
                .orElse(null);

        if (actual != null) {
            long minutosTranscurridos = ChronoUnit.MINUTES.between(actual.getFechaApertura(), LocalDateTime.now());
            
            if (minutosTranscurridos >= DURACION_CICLO_MINUTOS) {
                System.out.println(">>> SCHEDULER: Tiempo cumplido (" + minutosTranscurridos + " min). Cerrando Ciclo " + actual.getNumeroCiclo());
                try {
                    compensacionService.realizarCierreDiario(actual.getId());
                } catch (Exception e) {
                    System.err.println("Error en cierre automático: " + e.getMessage());
                }
            }
        }
    }
}
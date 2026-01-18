package com.bancario.compensacion.config;

import com.bancario.compensacion.model.CicloCompensacion;
import com.bancario.compensacion.repository.CicloCompensacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CicloCompensacionRepository cicloRepo;

    public DataInitializer(CicloCompensacionRepository cicloRepo) {
        this.cicloRepo = cicloRepo;
    }

    @Override
    public void run(String... args) {
        // Si no hay ningún ciclo, crea el Ciclo 1 (Semilla)
        if (cicloRepo.count() == 0) {
            CicloCompensacion c1 = new CicloCompensacion();
            c1.setNumeroCiclo(1);
            c1.setDescripcion("Ciclo Inicial - Arranque del Switch");
            c1.setEstado("ABIERTO");
            c1.setFechaApertura(LocalDateTime.now());
            cicloRepo.save(c1);
            System.out.println(">>> INICIALIZADOR: Ciclo 1 Creado Automáticamente.");
        }
    }
}
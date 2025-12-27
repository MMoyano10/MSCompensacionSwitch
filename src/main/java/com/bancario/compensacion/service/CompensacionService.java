package com.bancario.compensacion.service;

import com.bancario.compensacion.dto.ArchivoDTO;
import com.bancario.compensacion.dto.CicloDTO;
import com.bancario.compensacion.dto.PosicionDTO;
import com.bancario.compensacion.exception.ResourceNotFoundException;
import com.bancario.compensacion.model.ArchivoLiquidacion;
import com.bancario.compensacion.model.CicloCompensacion;
import com.bancario.compensacion.model.PosicionInstitucion;
import com.bancario.compensacion.repository.ArchivoLiquidacionRepository;
import com.bancario.compensacion.repository.CicloCompensacionRepository;
import com.bancario.compensacion.repository.PosicionInstitucionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompensacionService {

    private final CicloCompensacionRepository cicloRepository;
    private final PosicionInstitucionRepository posicionRepository;
    private final ArchivoLiquidacionRepository archivoRepository;

    @Transactional(readOnly = true)
    public List<CicloDTO> listarCiclos() {
        log.info("Listando todos los ciclos de compensación");
        return cicloRepository.findAll().stream()
                .map(this::mapToCicloDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CicloDTO crearCiclo(CicloDTO dto) {
        log.info("Creando nuevo ciclo de compensación para fecha: {}", dto.getFechaCorte());
        CicloCompensacion entity = new CicloCompensacion();
        entity.setFechaCorte(dto.getFechaCorte());
        entity.setEstado(dto.getEstado());
        entity.setDescripcion(dto.getDescripcion());
        return mapToCicloDTO(cicloRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public CicloDTO obtenerCiclo(Integer id) {
        log.info("Obteniendo ciclo con ID: {}", id);
        return cicloRepository.findById(id)
                .map(this::mapToCicloDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo no encontrado con ID: " + id));
    }

    @Transactional
    public PosicionDTO registrarPosicion(PosicionDTO dto) {
        log.info("Registrando posición para BIC: {} en ciclo: {}", dto.getCodigoBic(), dto.getIdCiclo());
        CicloCompensacion ciclo = cicloRepository.findById(dto.getIdCiclo())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo no encontrado con ID: " + dto.getIdCiclo()));

        PosicionInstitucion entity = new PosicionInstitucion();
        entity.setCiclo(ciclo);
        entity.setCodigoBic(dto.getCodigoBic());
        entity.setTotalDebitos(dto.getTotalDebitos());
        entity.setTotalCreditos(dto.getTotalCreditos());
        entity.setNeto(dto.getNeto());

        return mapToPosicionDTO(posicionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<PosicionDTO> listarPosicionesPorCiclo(Integer cicloId) {
        log.info("Listando posiciones para el ciclo: {}", cicloId);
        return posicionRepository.findByCicloId(cicloId).stream()
                .map(this::mapToPosicionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ArchivoDTO registrarArchivo(ArchivoDTO dto) {
        log.info("Registrando archivo: {} para ciclo: {}", dto.getNombre(), dto.getIdCiclo());
        CicloCompensacion ciclo = cicloRepository.findById(dto.getIdCiclo())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo no encontrado con ID: " + dto.getIdCiclo()));

        ArchivoLiquidacion entity = new ArchivoLiquidacion();
        entity.setCiclo(ciclo);
        entity.setNombre(dto.getNombre());
        entity.setCanalEnvio(dto.getCanalEnvio());
        entity.setEstado(dto.getEstado());
        entity.setFechaGeneracion(dto.getFechaGeneracion());

        return mapToArchivoDTO(archivoRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ArchivoDTO> listarArchivosPorCiclo(Integer cicloId) {
        log.info("Listando archivos para el ciclo: {}", cicloId);
        return archivoRepository.findByCicloId(cicloId).stream()
                .map(this::mapToArchivoDTO)
                .collect(Collectors.toList());
    }

    private CicloDTO mapToCicloDTO(CicloCompensacion entity) {
        return CicloDTO.builder()
                .id(entity.getId())
                .fechaCorte(entity.getFechaCorte())
                .estado(entity.getEstado())
                .descripcion(entity.getDescripcion())
                .build();
    }

    private PosicionDTO mapToPosicionDTO(PosicionInstitucion entity) {
        return PosicionDTO.builder()
                .id(entity.getId())
                .idCiclo(entity.getCiclo().getId())
                .codigoBic(entity.getCodigoBic())
                .totalDebitos(entity.getTotalDebitos())
                .totalCreditos(entity.getTotalCreditos())
                .neto(entity.getNeto())
                .build();
    }

    private ArchivoDTO mapToArchivoDTO(ArchivoLiquidacion entity) {
        return ArchivoDTO.builder()
                .id(entity.getId())
                .idCiclo(entity.getCiclo().getId())
                .nombre(entity.getNombre())
                .canalEnvio(entity.getCanalEnvio())
                .estado(entity.getEstado())
                .fechaGeneracion(entity.getFechaGeneracion())
                .build();
    }
}

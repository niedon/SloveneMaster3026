package com.bcadaval.esloveno.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.EstructuraFraseConfig;

/**
 * Repositorio para la configuración de estructuras de frase.
 */
@Repository
public interface EstructuraFraseConfigRepo extends JpaRepository<EstructuraFraseConfig, String> {

    /**
     * Encuentra todas las estructuras activas
     */
    List<EstructuraFraseConfig> findByActivaTrue();

    /**
     * Encuentra todas las estructuras inactivas
     */
    List<EstructuraFraseConfig> findByActivaFalse();
}


package com.bcadaval.esloveno.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.Sustantivo;

@Repository
public interface SustantivoRepo extends JpaRepository<Sustantivo, String> {

    /**
     * Busca sustantivos cuya forma principal contenga el texto (case-insensitive)
     */
    List<Sustantivo> findByPrincipalContainingIgnoreCase(String texto);

    /**
     * Busca sustantivos cuyo significado contenga el texto (case-insensitive)
     */
    List<Sustantivo> findBySignificadoContainingIgnoreCase(String texto);
}

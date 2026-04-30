package com.bcadaval.esloveno.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.Verbo;

@SuppressWarnings("unused")
@Repository
public interface VerboRepo extends JpaRepository<Verbo, String>{

    /**
     * Busca verbos cuya forma principal contenga el texto (case-insensitive)
     */
    List<Verbo> findByPrincipalContainingIgnoreCase(String texto);

    /**
     * Busca verbos cuyo significado contenga el texto (case-insensitive)
     */
    List<Verbo> findBySignificadoContainingIgnoreCase(String texto);
}

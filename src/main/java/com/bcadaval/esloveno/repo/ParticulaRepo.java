package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.palabra.Particula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticulaRepo extends JpaRepository<Particula, String> {

    /**
     * Busca partículas sin significado asignado.
     */
    List<Particula> findBySignificadoIsNull();

    /**
     * Busca una partícula por su forma principal.
     */
    Optional<Particula> findByPrincipal(String principal);

    /**
     * Busca partículas cuya forma principal contenga el texto (case-insensitive).
     */
    List<Particula> findByPrincipalContainingIgnoreCase(String texto);

    /**
     * Busca partículas cuyo significado contenga el texto (case-insensitive).
     */
    List<Particula> findBySignificadoContainingIgnoreCase(String texto);
}



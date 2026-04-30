package com.bcadaval.esloveno.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.Adjetivo;

@Repository
@SuppressWarnings("unused")
public interface AdjetivoRepo extends JpaRepository<Adjetivo, String>{

    /**
     * Encuentra adjetivos con significado null
     */
    List<Adjetivo> findBySignificadoIsNull();

    /**
     * Busca adjetivos cuya forma principal contenga el texto (case-insensitive)
     */
    List<Adjetivo> findByPrincipalContainingIgnoreCase(String texto);

    /**
     * Busca adjetivos cuyo significado contenga el texto (case-insensitive)
     */
    List<Adjetivo> findBySignificadoContainingIgnoreCase(String texto);
}

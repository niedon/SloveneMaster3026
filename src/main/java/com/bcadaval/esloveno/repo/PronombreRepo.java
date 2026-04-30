package com.bcadaval.esloveno.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bcadaval.esloveno.beans.palabra.Pronombre;

import java.util.List;

@SuppressWarnings("unused")
public interface PronombreRepo extends JpaRepository<Pronombre, String> {

    List<Pronombre> findBySignificadoIsNull();

    /**
     * Busca pronombres cuya forma principal contenga el texto (case-insensitive)
     */
    List<Pronombre> findByPrincipalContainingIgnoreCase(String texto);

    /**
     * Busca pronombres cuyo significado contenga el texto (case-insensitive)
     */
    List<Pronombre> findBySignificadoContainingIgnoreCase(String texto);

}

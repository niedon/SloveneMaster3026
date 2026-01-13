package com.bcadaval.esloveno.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.Sustantivo;

@Repository
public interface SustantivoRepo extends JpaRepository<Sustantivo, String> {

    /**
     * Encuentra sustantivos con significado null o animado null
     */
    List<Sustantivo> findBySignificadoIsNullOrAnimadoIsNull();
}

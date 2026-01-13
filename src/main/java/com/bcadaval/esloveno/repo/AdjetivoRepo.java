package com.bcadaval.esloveno.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.Adjetivo;

@Repository
public interface AdjetivoRepo extends JpaRepository<Adjetivo, String>{

    /**
     * Encuentra adjetivos con significado null
     */
    List<Adjetivo> findBySignificadoIsNull();
}

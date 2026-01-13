package com.bcadaval.esloveno.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.Verbo;

@Repository
public interface VerboRepo extends JpaRepository<Verbo, String>{

    /**
     * Encuentra verbos con significado null o transitividad null
     */
    List<Verbo> findBySignificadoIsNullOrTransitividadIsNull();
}

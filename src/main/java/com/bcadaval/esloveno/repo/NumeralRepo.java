package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.palabra.Numeral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NumeralRepo extends JpaRepository<Numeral, String> {

    List<Numeral> findBySignificadoIsNull();

    /**
     * Busca numerales incompletos: sin significado o sin cantidad asignada.
     */
    List<Numeral> findBySignificadoIsNullOrCantidadIsNull();

    /**
     * Busca numerales cuya forma principal contenga el texto (case-insensitive)
     */
    List<Numeral> findByPrincipalContainingIgnoreCase(String texto);

    /**
     * Busca numerales cuyo significado contenga el texto (case-insensitive)
     */
    List<Numeral> findBySignificadoContainingIgnoreCase(String texto);

}



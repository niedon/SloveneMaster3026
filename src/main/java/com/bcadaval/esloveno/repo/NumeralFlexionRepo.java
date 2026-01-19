package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;

public interface NumeralFlexionRepo extends JpaRepository<NumeralFlexion, Integer>, QueryByExampleExecutor<NumeralFlexion> {

    List<NumeralFlexion> findByPrincipal(String principal);

    /**
     * Busca numerales que coincidan en caso y número, opcionalmente filtrando por género.
     * Solo devuelve tarjetas inicializadas (proximaRevision IS NOT NULL).
     *
     * @param caso Caso gramatical requerido
     * @param numero Número gramatical requerido
     * @param genero Género gramatical requerido (puede ser null)
     * @return Lista de flexiones que coinciden con los criterios
     */
    @Query("SELECT nf FROM NumeralFlexion nf " +
            "WHERE nf.caso = :caso " +
            "AND nf.numero = :numero " +
            "AND (:genero IS NULL OR nf.genero = :genero) " +
            "AND nf.proximaRevision IS NOT NULL")
    List<NumeralFlexion> findByCasoAndNumeroAndGenero(
            @Param("caso") Caso caso,
            @Param("numero") Numero numero,
            @Param("genero") Genero genero
    );
}


package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NumeralFlexionRepo extends FlexionBaseRepo<NumeralFlexion, Integer> {

    /**
     * Busca numerales que coincidan en caso y número, opcionalmente filtrando por género.
     * Solo devuelve tarjetas inicializadas (proximaRevision IS NOT NULL).
     * <p>
     * <strong>Uso:</strong> consultas SRS (palabras disponibles para estudio).
     * Para generadores usar {@link #findByCasoAndNumeroAndGeneroSinFiltroSRS}.
     *
     * @param caso   Caso gramatical requerido
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

    /**
     * Busca numerales que coincidan en caso y número, opcionalmente filtrando por género.
     * NO filtra por campos SRS: devuelve tanto palabras nuevas como ya estudiadas.
     * <p>
     * <strong>Uso exclusivo de generadores</strong>, donde la palabra no participa en SRS
     * y por tanto no debe excluirse por su estado de revisión.
     *
     * @param caso   Caso gramatical requerido
     * @param numero Número gramatical requerido
     * @param genero Género gramatical requerido (puede ser null)
     * @return Lista de flexiones que coinciden con los criterios
     */
    @Query("SELECT nf FROM NumeralFlexion nf " +
            "WHERE nf.caso = :caso " +
            "AND nf.numero = :numero " +
            "AND (:genero IS NULL OR nf.genero = :genero)")
    List<NumeralFlexion> findByCasoAndNumeroAndGeneroSinFiltroSRS(
            @Param("caso") Caso caso,
            @Param("numero") Numero numero,
            @Param("genero") Genero genero
    );
}


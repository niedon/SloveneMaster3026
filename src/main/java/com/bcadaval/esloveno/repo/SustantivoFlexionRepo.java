package com.bcadaval.esloveno.repo;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;

@Repository
public interface SustantivoFlexionRepo extends JpaRepository<SustantivoFlexion, Integer> {

	/**
	 * Stream de sustantivos listos para estudiar: activos y con tiempo cumplido
	 */
	@Query("SELECT s FROM SustantivoFlexion s WHERE s.proximaRevision IS NOT NULL AND s.proximaRevision <= :ahora")
	Stream<SustantivoFlexion> streamListosParaEstudiar(@Param("ahora") Instant ahora);

	/**
	 * Stream de todos los sustantivos activos (tienen proximaRevision)
	 */
	@Query("SELECT s FROM SustantivoFlexion s WHERE s.proximaRevision IS NOT NULL")
	Stream<SustantivoFlexion> streamActivos();

	/**
	 * Encuentra flexiones por el principal (sustantivo base)
	 */
	List<SustantivoFlexion> findByPrincipal(String principal);

	/**
	 * Busca sustantivos que coincidan en caso, número y género.
	 * Filtra en la base de datos haciendo JOIN con la tabla principal (Sustantivo).
	 * Solo devuelve tarjetas inicializadas (proximaRevision IS NOT NULL).
	 *
	 * @param caso Caso gramatical requerido
	 * @param numero Número gramatical requerido
	 * @param genero Género gramatical requerido (del sustantivo base)
	 * @return Lista de flexiones que coinciden con los criterios
	 */
	@Query("SELECT sf FROM SustantivoFlexion sf " +
			"INNER JOIN Sustantivo s ON sf.principal = s.principal " +
			"WHERE sf.caso = :caso " +
			"AND sf.numero = :numero " +
			"AND s.genero = :genero " +
			"AND sf.proximaRevision IS NOT NULL")
	List<SustantivoFlexion> findByCasoAndNumeroAndGenero(
			@Param("caso") Caso caso,
			@Param("numero") Numero numero,
			@Param("genero") Genero genero
	);
}

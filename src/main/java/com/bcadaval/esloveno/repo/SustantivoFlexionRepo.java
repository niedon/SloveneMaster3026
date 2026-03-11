package com.bcadaval.esloveno.repo;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import com.bcadaval.esloveno.beans.enums.CabezaRelacional;
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

	/* ========================================================================== */
	/* ============================ MÉTODOS ESTÁNDAR ============================ */
	/* ========================================================================== */
	/**
	 * Encuentra flexiones por el sloleksId (sustantivo base)
	 */
	List<SustantivoFlexion> findBySloleksId(String sloleksId);


	/* ========================================================================== */
	/* ============================= MÉTODOS QUERY ============================== */
	/* ========================================================================== */

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
	 * Stream de sustantivos nuevos: proximaRevision IS NULL y palabra base completa
	 * (significado, animacidad, contabilidad, claseSemantica y cabezaRelacional no null).
	 */
	@Query("SELECT sf FROM SustantivoFlexion sf JOIN sf.sustantivoBase s " +
			"WHERE sf.proximaRevision IS NULL " +
			"AND s.significado IS NOT NULL " +
			"AND s.animacidad IS NOT NULL " +
			"AND s.contabilidad IS NOT NULL " +
			"AND s.claseSemantica IS NOT NULL " +
			"AND s.cabezaRelacional IS NOT NULL")
	Stream<SustantivoFlexion> streamNuevos();

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
			"INNER JOIN Sustantivo s ON sf.sloleksId = s.sloleksId " +
			"WHERE sf.caso = :caso " +
			"AND sf.numero = :numero " +
			"AND s.genero = :genero " +
			"AND sf.proximaRevision IS NOT NULL")
	List<SustantivoFlexion> findByCasoAndNumeroAndGenero(
			@Param("caso") Caso caso,
			@Param("numero") Numero numero,
			@Param("genero") Genero genero
	);

	/**
	 * Devuelve todas las flexiones cuya palabra base tiene el valor de
	 * {@code cabezaRelacional} indicado, independientemente de si la tarjeta está activa.
	 *
	 * @param cabezaRelacional valor requerido de cabeza relacional
	 * @return lista de flexiones que cumplen el criterio
	 */
	@Query("SELECT sf FROM SustantivoFlexion sf JOIN sf.sustantivoBase s " +
			"WHERE s.cabezaRelacional = :cabezaRelacional " +
			"AND sf.caso = :caso")
	List<SustantivoFlexion> findByCabezaRelacional(
			@Param("caso") Caso caso,
			@Param("cabezaRelacional") CabezaRelacional cabezaRelacional);

	List<SustantivoFlexion> findByCaso(Caso caso);
}

package com.bcadaval.esloveno.repo;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;

@Repository
public interface AdjetivoFlexionRepo extends FlexionBaseRepo<AdjetivoFlexion, Integer> {

	/**
	 * Stream de adjetivos listos para estudiar: activos y con tiempo cumplido
	 */
	@Query("SELECT a FROM AdjetivoFlexion a WHERE a.proximaRevision IS NOT NULL AND a.proximaRevision <= :ahora")
	Stream<AdjetivoFlexion> streamListosParaEstudiar(@Param("ahora") Instant ahora);

	/**
	 * Stream de todos los adjetivos activos (tienen proximaRevision)
	 */
	@Query("SELECT a FROM AdjetivoFlexion a WHERE a.proximaRevision IS NOT NULL")
	Stream<AdjetivoFlexion> streamActivos();

	/**
	 * Stream de adjetivos nuevos: proximaRevision IS NULL y palabra base completa
	 * (significado no null).
	 */
	@Query("SELECT af FROM AdjetivoFlexion af JOIN af.adjetivoBase a " +
			"WHERE af.proximaRevision IS NULL " +
			"AND a.significado IS NOT NULL")
	Stream<AdjetivoFlexion> streamNuevos();

	/**
	 * Encuentra flexiones por el sloleksId (adjetivo base)
	 */
	List<AdjetivoFlexion> findBySloleksId(String sloleksId);
}

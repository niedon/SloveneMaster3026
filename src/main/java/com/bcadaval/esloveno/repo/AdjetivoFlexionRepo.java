package com.bcadaval.esloveno.repo;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;

@Repository
public interface AdjetivoFlexionRepo extends JpaRepository<AdjetivoFlexion, Integer> {

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
	 * Encuentra flexiones por el principal (adjetivo base)
	 */
	List<AdjetivoFlexion> findByPrincipal(String principal);
}

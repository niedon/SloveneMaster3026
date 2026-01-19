package com.bcadaval.esloveno.repo;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

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
}

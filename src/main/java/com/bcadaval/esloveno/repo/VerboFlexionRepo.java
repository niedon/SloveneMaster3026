package com.bcadaval.esloveno.repo;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.VerboFlexion;

@Repository
public interface VerboFlexionRepo extends JpaRepository<VerboFlexion, Integer> {

	/**
	 * Stream de verbos listos para estudiar: activos (proximaRevision != null) y con tiempo cumplido
	 */
	@Query("SELECT v FROM VerboFlexion v WHERE v.proximaRevision IS NOT NULL AND v.proximaRevision <= :ahora")
	Stream<VerboFlexion> streamListosParaEstudiar(@Param("ahora") Instant ahora);

	/**
	 * Stream de todos los verbos activos (tienen proximaRevision)
	 */
	@Query("SELECT v FROM VerboFlexion v WHERE v.proximaRevision IS NOT NULL")
	Stream<VerboFlexion> streamActivos();

	/**
	 * Encuentra flexiones por el principal (verbo base)
	 */
	List<VerboFlexion> findByPrincipal(String principal);


}


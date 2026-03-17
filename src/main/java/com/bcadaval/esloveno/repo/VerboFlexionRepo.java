package com.bcadaval.esloveno.repo;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.VerboFlexion;

@Repository
public interface VerboFlexionRepo extends FlexionBaseRepo<VerboFlexion, Integer> {

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
	 * Stream de verbos nuevos: proximaRevision IS NULL y palabra base completa
	 * (significado, transitividad, requiereSujetoAnimado y requiereObjetoAnimado no null).
	 */
	@Query("SELECT vf FROM VerboFlexion vf JOIN vf.verboBase v " +
			"WHERE vf.proximaRevision IS NULL " +
			"AND v.significado IS NOT NULL " +
			"AND v.transitividad IS NOT NULL " +
			"AND v.requiereSujetoAnimado IS NOT NULL " +
			"AND v.requiereObjetoAnimado IS NOT NULL")
	Stream<VerboFlexion> streamNuevos();

	/**
	 * Encuentra flexiones por el sloleksId (verbo base)
	 */
	List<VerboFlexion> findBySloleksId(String sloleksId);


}


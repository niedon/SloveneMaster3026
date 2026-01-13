package com.bcadaval.esloveno.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;

@Repository
public interface AdjetivoFlexionRepo extends JpaRepository<AdjetivoFlexion, Integer>{

	/**
	 * Encuentra flexiones por el principal (adjetivo base)
	 */
	List<AdjetivoFlexion> findByPrincipal(String principal);

	/**
	 * Encuentra tarjetas nuevas (nunca estudiadas)
	 */
	List<AdjetivoFlexion> findByProximaRevisionIsNull();
}

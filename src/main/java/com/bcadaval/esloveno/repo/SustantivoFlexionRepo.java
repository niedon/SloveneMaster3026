package com.bcadaval.esloveno.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;

@Repository
public interface SustantivoFlexionRepo extends JpaRepository<SustantivoFlexion, Integer>, QueryByExampleExecutor<SustantivoFlexion> {

	/**
	 * Encuentra flexiones por el principal (sustantivo base)
	 */
	List<SustantivoFlexion> findByPrincipal(String principal);

	/**
	 * Encuentra tarjetas nuevas (nunca estudiadas)
	 */
	List<SustantivoFlexion> findByProximaRevisionIsNull();
}

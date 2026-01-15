package com.bcadaval.esloveno.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;

@Repository
public interface VerboFlexionRepo extends JpaRepository<VerboFlexion, Integer>,
        JpaSpecificationExecutor<VerboFlexion> {

	/**
	 * Encuentra flexiones por el principal (verbo base)
	 */
	List<VerboFlexion> findByPrincipal(String principal);

	/**
	 * Encuentra flexiones por el principal y forma verbal
	 */
	List<VerboFlexion> findByPrincipalAndFormaVerbal(String principal, FormaVerbal formaVerbal);

	/**
	 * Encuentra tarjetas nuevas (nunca estudiadas)
	 */
	List<VerboFlexion> findByProximaRevisionIsNull();

	/**
	 * Encuentra flexiones por forma verbal
	 */
	List<VerboFlexion> findByFormaVerbal(FormaVerbal formaVerbal);
}


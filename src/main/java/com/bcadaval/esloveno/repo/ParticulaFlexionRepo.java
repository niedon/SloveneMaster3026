package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.palabra.ParticulaFlexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public interface ParticulaFlexionRepo extends JpaRepository<ParticulaFlexion, Integer>, QueryByExampleExecutor<ParticulaFlexion> {

    List<ParticulaFlexion> findBySloleksId(String sloleksId);

    /**
     * Busca flexiones de partícula cuya forma principal coincide.
     */
    List<ParticulaFlexion> findByPrincipal(String principal);

    /**
     * Stream de partículas listas para estudiar: activas y con tiempo cumplido.
     */
    @Query("SELECT p FROM ParticulaFlexion p WHERE p.proximaRevision IS NOT NULL AND p.proximaRevision <= :ahora")
    Stream<ParticulaFlexion> streamListosParaEstudiar(@Param("ahora") Instant ahora);

    /**
     * Stream de todas las partículas activas (tienen proximaRevision).
     */
    @Query("SELECT p FROM ParticulaFlexion p WHERE p.proximaRevision IS NOT NULL")
    Stream<ParticulaFlexion> streamActivos();

    /**
     * Stream de partículas nuevas: proximaRevision IS NULL y palabra base completa
     * (significado no null).
     */
    @Query("SELECT pf FROM ParticulaFlexion pf JOIN pf.particulaBase p " +
            "WHERE pf.proximaRevision IS NULL " +
            "AND p.significado IS NOT NULL")
    Stream<ParticulaFlexion> streamNuevos();
}


package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public interface PronombreFlexionRepo extends JpaRepository<PronombreFlexion, Integer>, QueryByExampleExecutor<PronombreFlexion> {

    List<PronombreFlexion> findBySloleksId(String sloleksId);

    List<PronombreFlexion> findBySignificadoIsNull();

    /**
     * Stream de pronombres listos para estudiar: activos y con tiempo cumplido.
     */
    @Query("SELECT p FROM PronombreFlexion p WHERE p.proximaRevision IS NOT NULL AND p.proximaRevision <= :ahora")
    Stream<PronombreFlexion> streamListosParaEstudiar(@Param("ahora") Instant ahora);

    /**
     * Stream de todos los pronombres activos (tienen proximaRevision).
     */
    @Query("SELECT p FROM PronombreFlexion p WHERE p.proximaRevision IS NOT NULL")
    Stream<PronombreFlexion> streamActivos();

    /**
     * Stream de pronombres nuevos: proximaRevision IS NULL y significado no null.
     * El significado está directamente en la flexión para pronombres.
     */
    @Query("SELECT p FROM PronombreFlexion p WHERE p.proximaRevision IS NULL AND p.significado IS NOT NULL")
    Stream<PronombreFlexion> streamNuevos();
}

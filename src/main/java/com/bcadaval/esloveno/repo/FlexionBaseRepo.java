package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

@NoRepositoryBean
public interface FlexionBaseRepo<T extends PalabraFlexion<?>, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    List<T> findBySloleksId(String sloleksId);

    // noinspection JpaQlInspection
    @Modifying
    // #{#entityName} se resolverá dinámicamente según la entidad del repositorio hijo (ej. AdjetivoFlexion)
    @Query("UPDATE #{#entityName} e SET e.elegible = false")
    void resetElegibilidad();

    /**
     * Agrupa proximaRevision por formato y cuenta, usando la función nativa strftime de SQLite.
     * JPQL puro que delega la agrupación a la BD.
     */
    // noinspection JpaQlInspection
    @Query("SELECT function('strftime', :format, e.proximaRevision) as fecha, COUNT(e) " +
            "FROM #{#entityName} e " +
            "WHERE e.proximaRevision >= :inicio " +
            "AND e.proximaRevision <= :fin " +
            "AND e.elegible = true " +
            "GROUP BY function('strftime', :format, e.proximaRevision)")
    List<Object[]> countByProximaRevisionGrouped(
            @Param("inicio") Instant inicio,
            @Param("fin") Instant fin,
            @Param("format") String format
    );
}

package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

@NoRepositoryBean
public interface FlexionBaseRepo<T extends PalabraFlexion<?>, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    //TODO abstraer findBySloleksId

    @Modifying
    // #{#entityName} se resolverá dinámicamente según la entidad del repositorio hijo (ej. AdjetivoFlexion)
    @Query("UPDATE #{#entityName} e SET e.elegible = false")
    void resetElegibilidad();

    /**
     * Marca como elegibles (true) solo las flexiones cuyos IDs estén en la lista proporcionada.
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.elegible = true WHERE e.id IN :ids")
    void markAsElegible(@Param("ids") Collection<ID> ids);
    
}

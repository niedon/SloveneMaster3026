package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface FlexionBaseRepo<T extends PalabraFlexion<?>, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    List<T> findBySloleksId(String sloleksId);

    @Modifying
    // #{#entityName} se resolverá dinámicamente según la entidad del repositorio hijo (ej. AdjetivoFlexion)
    @Query("UPDATE #{#entityName} e SET e.elegible = false")
    void resetElegibilidad();
    
}

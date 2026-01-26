package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;

public interface PronombreFlexionRepo extends JpaRepository<PronombreFlexion, Integer>, QueryByExampleExecutor<PronombreFlexion> {

    List<PronombreFlexion> findBySloleksId(String sloleksId);

    List<PronombreFlexion> findBySignificadoIsNull();
}

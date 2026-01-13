package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;

public interface NumeralFlexionRepo extends JpaRepository<NumeralFlexion, Integer>, QueryByExampleExecutor<NumeralFlexion> {

    List<NumeralFlexion> findByPrincipal(String principal);
}


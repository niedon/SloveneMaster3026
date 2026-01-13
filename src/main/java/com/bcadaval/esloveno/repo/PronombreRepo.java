package com.bcadaval.esloveno.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bcadaval.esloveno.beans.palabra.Pronombre;

import java.util.List;

public interface PronombreRepo extends JpaRepository<Pronombre, String> {

    List<Pronombre> findBySignificadoIsNull();

}

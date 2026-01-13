package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.palabra.Numeral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NumeralRepo extends JpaRepository<Numeral, String> {

    List<Numeral> findBySignificadoIsNull();

}



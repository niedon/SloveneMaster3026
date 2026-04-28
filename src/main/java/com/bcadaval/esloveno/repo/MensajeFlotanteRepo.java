package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.MensajeFlotante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeFlotanteRepo extends JpaRepository<MensajeFlotante, Long> {
    List<MensajeFlotante> findByLeidoFalseOrderByFechaAsc();
}


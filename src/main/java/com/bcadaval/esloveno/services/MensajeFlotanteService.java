package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.MensajeFlotante;
import com.bcadaval.esloveno.repo.MensajeFlotanteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MensajeFlotanteService {

    @Autowired
    private MensajeFlotanteRepo repo;

    public static final int INTERVALO_REFRESCO_SEGUNDOS = 30;

    public MensajeFlotante insertarMensaje(String texto) {
        MensajeFlotante msg = MensajeFlotante.builder()
                .mensaje(texto)
                .fecha(LocalDateTime.now())
                .leido(false)
                .build();
        return repo.save(msg);
    }

    public List<MensajeFlotante> obtenerNoLeidos() {
        return repo.findByLeidoFalseOrderByFechaAsc();
    }

    public void marcarComoLeido(Long id) {
        repo.findById(id).ifPresent(msg -> {
            msg.setLeido(true);
            repo.save(msg);
        });
    }
}


package com.bcadaval.esloveno.services.palabra;

import com.bcadaval.esloveno.beans.palabra.ParticulaFlexion;
import com.bcadaval.esloveno.repo.ParticulaFlexionRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servicio para gestionar partículas.
 * <p>
 * Las partículas son palabras invariables (ej: "ne", "že", "baje").
 * Este servicio proporciona métodos para obtener flexiones de partículas,
 * principalmente para usar como generadores en las frases.
 */
@Log4j2
@Service
public class ParticulaService {

    @Autowired
    private ParticulaFlexionRepo particulaFlexionRepo;

    /**
     * Obtiene una flexión de partícula cuya forma principal coincide con la dada.
     * Si hay múltiples flexiones para la misma principal, devuelve una aleatoria.
     *
     * @param principal forma principal de la partícula (ej: "ne")
     * @return flexión de la partícula, o null si no se encuentra
     */
    public ParticulaFlexion getPorPrincipal(String principal) {
        List<ParticulaFlexion> candidatos = particulaFlexionRepo.findByPrincipal(principal);

        if (candidatos.isEmpty()) {
            log.warn("No se encontró ninguna flexión para la partícula '{}'", principal);
            return null;
        }

        return candidatos.get(ThreadLocalRandom.current().nextInt(candidatos.size()));
    }
}


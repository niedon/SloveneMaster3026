package com.bcadaval.esloveno.services.palabra;

import com.bcadaval.esloveno.beans.palabra.ParticulaFlexion;
import com.bcadaval.esloveno.repo.ParticulaFlexionRepo;
import com.bcadaval.esloveno.services.RandomEntitySelector;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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

    @Autowired
    private RandomEntitySelector randomSelector;

    /**
     * Obtiene una flexión de partícula cuya forma principal coincide con la dada.
     * Si hay múltiples flexiones para la misma principal, devuelve una aleatoria.
     * <p>
     * Toda la lógica de filtrado se ejecuta en BD.
     *
     * @param principal forma principal de la partícula (ej: "ne")
     * @return flexión de la partícula, o null si no se encuentra
     */
    public ParticulaFlexion getPorPrincipal(String principal) {
        Specification<ParticulaFlexion> spec = (root, query, cb) ->
                cb.equal(root.get("principal"), principal);

        return randomSelector.selectRandom(particulaFlexionRepo, spec).orElse(null);
    }
}

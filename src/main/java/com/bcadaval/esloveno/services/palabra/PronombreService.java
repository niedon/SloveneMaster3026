package com.bcadaval.esloveno.services.palabra;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.repo.PronombreFlexionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.beans.palabra.VerboFlexion;

@Service
public class PronombreService {

	@Autowired
	PronombreFlexionRepo pronombreFlexionRepo;

	/**
	 * Devuelve un pronombre nominativo no clítico que coincide con la persona y número del verbo dado.
	 * <p>
	 * Usa una query JPQL directa para evitar que {@code Example.of} filtre por los campos SRS
	 * con valores por defecto (factorFacilidad, intervaloRepeticionSegundos, etc.), lo que
	 * excluiría incorrectamente pronombres con historial SRS ya iniciado.
	 * </p>
	 */
	public PronombreFlexion getPronombre(VerboFlexion verboFlexion) {
		if (verboFlexion == null || verboFlexion.getPersona() == null || verboFlexion.getNumero() == null) {
			return null;
		}

		List<PronombreFlexion> candidatos = pronombreFlexionRepo.findByPersonaAndNumeroAndCasoAndNoClitico(
				verboFlexion.getPersona(),
				verboFlexion.getNumero(),
				Caso.NOMINATIVO
		);

		if (candidatos.isEmpty()) {
			return null;
		}
		return candidatos.get(ThreadLocalRandom.current().nextInt(candidatos.size()));
	}

}



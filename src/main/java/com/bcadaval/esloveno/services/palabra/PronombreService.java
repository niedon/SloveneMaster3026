package com.bcadaval.esloveno.services.palabra;

import java.util.concurrent.ThreadLocalRandom;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.repo.PronombreFlexionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.beans.palabra.VerboFlexion;

@Service
public class PronombreService {

	@Autowired
	PronombreFlexionRepo pronombreFlexionRepo;

	/** Devuelve un pronombre que coincide con la persona y número del verbo dado */
	public PronombreFlexion getPronombre(VerboFlexion verboFlexion) {
		if (verboFlexion == null || verboFlexion.getPersona() == null || verboFlexion.getNumero() == null) {
			return null;
		}
		var candidatos = pronombreFlexionRepo.findAll(
				Example.of(
						PronombreFlexion.builder()
							.persona(verboFlexion.getPersona())
							.numero(verboFlexion.getNumero())
							.caso(Caso.NOMINATIVO)
							.build())).stream()
		.filter(p -> !Boolean.TRUE.equals(p.getClitico())) // Excluir formas clíticas
		.toList();

		if (candidatos.isEmpty()) {
			return null;
		}
		// Seleccionar uno aleatorio
		return candidatos.get(ThreadLocalRandom.current().nextInt(candidatos.size()));
	}

}

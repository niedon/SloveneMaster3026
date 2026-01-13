package com.bcadaval.esloveno.services.palabra;

import java.util.concurrent.ThreadLocalRandom;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.repo.PronombreFlexionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.beans.palabra.Pronombre;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.PronombreRepo;

@Service
public class PronombreService {
	
	@Autowired
	PronombreRepo pronombreRepo;

	@Autowired
	PronombreFlexionRepo pronombreFlexionRepo;

	/** Devuelve un pronombre que coincide con la persona y número del verbo dado */
	public PronombreFlexion getPronombre(VerboFlexion verboFlexion) {
		return pronombreFlexionRepo.findAll(
				Example.of(
						PronombreFlexion.builder()
							.persona(verboFlexion.getPersona())
							.numero(verboFlexion.getNumero())
							.caso(Caso.NOMINATIVO)
							.build())).stream()
		.filter(p -> !Boolean.TRUE.equals(p.getClitico())) // Excluir formas clíticas
		.sorted((o1, o2) -> ThreadLocalRandom.current().nextInt(-1, 2))
		.findAny()
		.orElseThrow();
	}

}

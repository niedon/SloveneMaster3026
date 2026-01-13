package com.bcadaval.esloveno.services.palabra;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.Adjetivo;
import com.bcadaval.esloveno.beans.palabra.Numeral;
import com.bcadaval.esloveno.beans.palabra.Pronombre;
import com.bcadaval.esloveno.beans.palabra.Sustantivo;
import com.bcadaval.esloveno.beans.palabra.Verbo;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.repo.AdjetivoRepo;
import com.bcadaval.esloveno.repo.NumeralFlexionRepo;
import com.bcadaval.esloveno.repo.NumeralRepo;
import com.bcadaval.esloveno.repo.PronombreFlexionRepo;
import com.bcadaval.esloveno.repo.PronombreRepo;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoRepo;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.repo.VerboRepo;

@Log4j2
@Service
public class PalabraService {
	
	@Autowired
	private VerboRepo verboRepo;
	@Autowired
	private VerboFlexionRepo verboFlexionRepo;

	@Autowired
	private SustantivoRepo sustantivoRepo;
	@Autowired
	private SustantivoFlexionRepo sustantivoFlexionRepo;
	
	@Autowired
	private AdjetivoRepo adjetivoRepo;
	@Autowired
	private AdjetivoFlexionRepo adjetivoFlexionRepo;

	@Autowired
	private PronombreRepo pronombreRepo;
	@Autowired
	private PronombreFlexionRepo pronombreFlexionRepo;

	@Autowired
	private NumeralRepo numeralRepo;
	@Autowired
	private NumeralFlexionRepo numeralFlexionRepo;

	/** Devuelve el repositorio correspondiente según la clase de la palabra */
	private JpaRepository getRepository(Palabra palabra) {
		return switch(palabra) {
		case Verbo ignored -> verboRepo;
		case Adjetivo ignored -> adjetivoRepo;
		case Sustantivo ignored -> sustantivoRepo;
		case Pronombre ignored -> pronombreRepo;
		case Numeral ignored -> numeralRepo;
		default -> throw new IllegalArgumentException("Clase no soportada: " + palabra.getClass().getSimpleName());
		};
	}

	/** Devuelve el repositorio de flexiones correspondiente según la clase de la palabra */
	private JpaRepository getFlexionRepository(Palabra palabra) {
		return switch(palabra) {
		case Verbo ignored -> verboFlexionRepo;
		case Adjetivo ignored -> adjetivoFlexionRepo;
		case Sustantivo ignored -> sustantivoFlexionRepo;
		case Pronombre ignored -> pronombreFlexionRepo;
		case Numeral ignored -> numeralFlexionRepo;
		default -> throw new IllegalArgumentException("Clase no soportada: " + palabra.getClass().getSimpleName());
		};
	}

	/** Guarda una palabra y sus flexiones asociadas */
	public Palabra saveWordAndConjugations(Palabra palabra) {
		log.debug("Guardando palabra {} con {} flexiones", palabra.getPrincipal(), palabra.getListaFlexiones().size());
		Palabra palabraGuardada = (Palabra) getRepository(palabra).save(palabra);

		// Asignar la referencia a la palabra base en cada flexión
		for (Object flexion : palabra.getListaFlexiones()) {
			if (flexion instanceof PalabraFlexion pf) {
				pf.setPalabraBase(palabraGuardada);
			}
		}

		getFlexionRepository(palabra).saveAll(palabra.getListaFlexiones());

		return palabraGuardada;
	}

}

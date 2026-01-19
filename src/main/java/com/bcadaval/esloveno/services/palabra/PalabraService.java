package com.bcadaval.esloveno.services.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.TipoPalabra;
import com.bcadaval.esloveno.repo.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

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
	private JpaRepository getRepository(Palabra<?> palabra) {
		return switch (TipoPalabra.fromClass(palabra.getClass())){
			case SUSTANTIVO -> sustantivoRepo;
			case VERBO -> verboRepo;
			case ADJETIVO -> adjetivoRepo;
			case PRONOMBRE -> pronombreRepo;
			case NUMERAL -> numeralRepo;
		};
	}

	/** Devuelve el repositorio de flexiones correspondiente según la clase de la palabra */
	private JpaRepository getFlexionRepository(Palabra<?> palabra) {
		return switch (TipoPalabra.fromClass(palabra.getClass())){
            case SUSTANTIVO -> sustantivoFlexionRepo;
            case VERBO -> verboFlexionRepo;
            case ADJETIVO -> adjetivoFlexionRepo;
            case PRONOMBRE -> pronombreFlexionRepo;
            case NUMERAL -> numeralFlexionRepo;
        };

	}

	/** Guarda una palabra y sus flexiones asociadas */
	public Palabra<?> saveWordAndConjugations(Palabra<?> palabra) {
		log.debug("Guardando palabra {} con {} flexiones", palabra.getPrincipal(), palabra.getListaFlexiones().size());
		Palabra palabraGuardada = (Palabra<?>) getRepository(palabra).save(palabra);

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

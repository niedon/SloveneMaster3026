package com.bcadaval.esloveno.services.palabra.verbo;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.enums.Persona;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.beans.palabra.Verbo;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.repo.VerboRepo;

@Service
public class VerbosService {

	@Autowired
	private VerboRepo verboRepo;
	
	@Autowired
	private VerboFlexionRepo verboFlexionRepo;

	public List<Verbo> findAll() {
		return verboRepo.findAll();
	}
	
	public Verbo findById(String sloleksId) throws VerboNotFoundException {
		return verboRepo.findById(sloleksId).orElseThrow(VerboNotFoundException::new);
	}
	
	public boolean verbHasConjugations(String sloleksId) {
		return verboRepo.findById(sloleksId).isPresent() && verboFlexionRepo.count(Example.of(VerboFlexion.builder().sloleksId(sloleksId).build())) > 8;
	}
	
	public List<VerboFlexion> saveConjugations(List<VerboFlexion> conjugations) throws VerboNotFoundException {
		verboRepo.findById(conjugations.getFirst().getSloleksId()).orElseThrow(VerboNotFoundException::new);
		return verboFlexionRepo.saveAll(conjugations);
	}

	/**
	 * Obtiene un verbo transitivo en presente aleatorio de la base de datos.
	 * Útil como generador fallback para elementos opcionales.
	 *
	 * @return VerboFlexion transitivo en presente, o null si no hay ninguno disponible
	 */
	public VerboFlexion getVerboTransitivoPresenteAleatorio() {
		ExampleMatcher matcher = ExampleMatcher.matching()
				.withIgnoreNullValues()
				.withIgnorePaths(
						"factorFacilidad", "intervaloRepeticionSegundos",
						"vecesConsecutivasCorrectas", "totalRevisiones",
						"totalAciertos", "enReaprendizaje"
				);

		List<VerboFlexion> candidatos = verboFlexionRepo.findAll(
				Example.of(VerboFlexion.builder()
						.formaVerbal(FormaVerbal.PRESENT)
						.negativo(false)
						.build(), matcher))
				.stream()
				.filter(v -> v.getVerboBase() != null && v.getVerboBase().getTransitividad() == Transitividad.TRANSITIVO)
				.toList();

		if (candidatos.isEmpty()) {
			return null;
		}
		return candidatos.get(ThreadLocalRandom.current().nextInt(candidatos.size()));
	}

	/**
	 * Obtiene un verbo intransitivo o ambitransitivo en presente aleatorio de la base de datos,
	 * filtrado por persona y número.
	 * Útil como generador para estructuras de frase con numerales.
	 *
	 * @param persona Persona gramatical requerida (ej: TERCERA)
	 * @param numero Número gramatical requerido (SINGULAR, DUAL, PLURAL)
	 * @return VerboFlexion intransitivo/ambitransitivo en presente, o null si no hay ninguno disponible
	 */
	public VerboFlexion getVerboIntransitivoPresenteAleatorio(Persona persona, Numero numero) {
		ExampleMatcher matcher = ExampleMatcher.matching()
				.withIgnoreNullValues()
				.withIgnorePaths(
						"factorFacilidad", "intervaloRepeticionSegundos",
						"vecesConsecutivasCorrectas", "totalRevisiones",
						"totalAciertos", "enReaprendizaje"
				);

		List<VerboFlexion> candidatos = verboFlexionRepo.findAll(
				Example.of(VerboFlexion.builder()
						.formaVerbal(FormaVerbal.PRESENT)
						.persona(persona)
						.numero(numero)
						.negativo(false)
						.build(), matcher))
				.stream()
				.filter(v -> v.getVerboBase() != null
						&& (v.getVerboBase().getTransitividad() == Transitividad.INTRANSITIVO
						|| v.getVerboBase().getTransitividad() == Transitividad.AMBITRANSITIVO))
				.toList();

		if (candidatos.isEmpty()) {
			return null;
		}
		return candidatos.get(ThreadLocalRandom.current().nextInt(candidatos.size()));
	}
}

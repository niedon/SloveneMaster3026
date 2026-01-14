package com.bcadaval.esloveno.services.palabra;

import java.util.concurrent.ThreadLocalRandom;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.repo.NumeralFlexionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.repo.NumeralRepo;

@Service
public class NumeralService {

	@Autowired
	NumeralRepo numeralRepo;

	@Autowired
	NumeralFlexionRepo numeralFlexionRepo;

	/**
	 * Devuelve un numeral que coincida con el género, número y caso del sustantivo dado.
	 * Si el género es null, busca cualquier género.
	 * - Si numero es SINGULAR: principal debe ser "en"
	 * - Si numero es DUAL: principal debe ser "dva"
	 * - Si numero es PLURAL: principal debe ser distinto de "en" y "dva"
	 */
	public NumeralFlexion getNumeral(SustantivoFlexion sustantivoFlexion) {
		// Primero intentar buscar con el género del sustantivo
		Genero generoSustantivo = sustantivoFlexion.getSustantivoBase() != null
				? sustantivoFlexion.getSustantivoBase().getGenero()
				: null;

		// Determinar el principal basado en el número
		Numero numero = sustantivoFlexion.getNumero();
		Caso caso = sustantivoFlexion.getCaso();

		// Obtener resultados con género específico
		var resultados = numeralFlexionRepo.findAll(
				Example.of(
						NumeralFlexion.builder()
							.numero(numero)
							.caso(caso)
							.genero(generoSustantivo)
							.build())).stream()
		.filter(nf -> filterByPrincipal(nf, numero))
		.sorted((o1, o2) -> ThreadLocalRandom.current().nextInt(-1, 2));

		var resultado = resultados.findAny();

		// Si no hay con género específico, buscar sin género
		return resultado.orElseGet(() -> numeralFlexionRepo.findAll(
				Example.of(
						NumeralFlexion.builder()
							.numero(numero)
							.caso(caso)
							.build())).stream()
				.filter(nf -> filterByPrincipal(nf, numero))
				.sorted((o1, o2) -> ThreadLocalRandom.current().nextInt(-1, 2))
				.findAny()
				.orElse(null));
	}

	/**
	 * Filtra los numerales según el número:
	 * - SINGULAR: principal debe ser "en"
	 * - DUAL: principal debe ser "dva"
	 * - PLURAL: principal debe ser distinto de "en" y "dva"
	 */
	private boolean filterByPrincipal(NumeralFlexion nf, Numero numero) {
		if (numero == null || nf.getPrincipal() == null) {
			return false;
		}

		return switch (numero) {
			case SINGULAR -> "en".equals(nf.getPrincipal());
			case DUAL -> "dva".equals(nf.getPrincipal());
			case PLURAL -> !"en".equals(nf.getPrincipal()) && !"dva".equals(nf.getPrincipal());
		};
	}

	/**
	 * Devuelve un numeral que coincida con el número, caso y género dados
	 */
	public NumeralFlexion getNumeral(Numero numero, Caso caso, Genero genero) {
		return numeralFlexionRepo.findAll(
				Example.of(
						NumeralFlexion.builder()
							.numero(numero)
							.caso(caso)
							.genero(genero)
							.build())).stream()
		.sorted((o1, o2) -> ThreadLocalRandom.current().nextInt(-1, 2))
		.findAny()
		.orElseGet(() ->
			// Si no hay con género específico, buscar sin género
			numeralFlexionRepo.findAll(
					Example.of(
							NumeralFlexion.builder()
								.numero(numero)
								.caso(caso)
								.build())).stream()
			.sorted((o1, o2) -> ThreadLocalRandom.current().nextInt(-1, 2))
			.findAny()
			.orElse(null)
		);
	}

}


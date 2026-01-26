package com.bcadaval.esloveno.services.palabra;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.repo.NumeralFlexionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.repo.NumeralRepo;

@Service
public class NumeralService {

	@Autowired
	NumeralRepo numeralRepo;

	@Autowired
	NumeralFlexionRepo numeralFlexionRepo;

	/**
	 * Devuelve un numeral que coincida con el adjetivo dado.
	 */
	public NumeralFlexion getNumeral(AdjetivoFlexion adjetivoFlexion) {
		return getNumeral(
				adjetivoFlexion.getNumero(),
				adjetivoFlexion.getCaso(),
				adjetivoFlexion.getGenero()
		);
	}

	/**
	 * Devuelve un numeral que coincida con el género, número y caso del sustantivo dado.
	 */
	public NumeralFlexion getNumeral(SustantivoFlexion sustantivoFlexion) {
		return getNumeral(
				sustantivoFlexion.getNumero(),
				sustantivoFlexion.getCaso(),
				sustantivoFlexion.getSustantivoBase().getGenero()
		);
	}

	/**
	 * Devuelve un numeral que coincida con el número, caso y género dados.
	 * Lógica unificada que filtra en la base de datos y solo devuelve tarjetas inicializadas.
	 * - Si numero es SINGULAR: principal debe ser "en"
	 * - Si numero es DUAL: principal debe ser "dva"
	 * - Si numero es PLURAL: principal debe ser distinto de "en" y "dva"
	 *
	 * @param numero Número gramatical requerido
	 * @param caso Caso gramatical requerido
	 * @param genero Género gramatical requerido (puede ser null)
	 * @return NumeralFlexion que coincide, o null si no se encuentra
	 */
	public NumeralFlexion getNumeral(Numero numero, Caso caso, Genero genero) {
		// Primero intentar con género específico
		List<NumeralFlexion> candidatos = numeralFlexionRepo.findByCasoAndNumeroAndGenero(caso, numero, genero);

		// Filtrar por principal según el número
		List<NumeralFlexion> filtrados = candidatos.stream()
				.filter(nf -> filterByPrincipal(nf, numero))
				.toList();

		if (filtrados.isEmpty()) {
			return null;
		}

		// Devolver uno aleatorio
		return filtrados.get(ThreadLocalRandom.current().nextInt(filtrados.size()));
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

		String principal = nf.getPrincipal();
		return switch (numero) {
			case SINGULAR -> "en".equals(principal);
			case DUAL -> "dva".equals(principal);
			case PLURAL -> !"en".equals(principal) && !"dva".equals(principal);
		};
	}

}


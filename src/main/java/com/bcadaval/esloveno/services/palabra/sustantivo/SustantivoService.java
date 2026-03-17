package com.bcadaval.esloveno.services.palabra.sustantivo;

import java.util.NoSuchElementException;

import com.bcadaval.esloveno.beans.enums.Animacidad;
import com.bcadaval.esloveno.beans.enums.CabezaRelacional;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.services.RandomEntitySelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar sustantivos y sus flexiones.
 * <p>
 * Proporciona métodos semánticos para obtener sustantivos según criterios
 * gramaticales, delegando toda la lógica de filtrado a Specifications JPA.
 */
@Service
public class SustantivoService {
	
	@Autowired
	private SustantivoFlexionRepo sustantivoFlexionRepo;

	@Autowired
	private RandomEntitySelector randomSelector;

	/**
	 * Obtiene un sustantivo aleatorio de toda la base de datos.
	 *
	 * @return flexión aleatoria de sustantivo
	 * @throws NoSuchElementException si no hay sustantivos disponibles
	 */
	public SustantivoFlexion getSustantivoAleatorio() throws NoSuchElementException {
		return randomSelector.selectRandom(sustantivoFlexionRepo)
				.orElseThrow(() -> new NoSuchElementException("No hay sustantivos disponibles"));
	}

	/**
	 * Obtiene un sustantivo que coincida en caso, género y número con el adjetivo dado.
	 * El género se obtiene del sustantivo base.
	 * Solo devuelve sustantivos con tarjetas inicializadas (proximaRevision IS NOT NULL).
	 *
	 * @param adjetivoFlexion Adjetivo con el que debe concordar el sustantivo
	 * @return SustantivoFlexion que concuerda con el adjetivo
	 * @throws NoSuchElementException si no se encuentra ningún sustantivo que concuerde
	 */
	public SustantivoFlexion getSustantivo(AdjetivoFlexion adjetivoFlexion) throws NoSuchElementException {
		Specification<SustantivoFlexion> spec = (root, query, cb) -> cb.and(
				cb.equal(root.get("caso"), adjetivoFlexion.getCaso()),
				cb.equal(root.get("numero"), adjetivoFlexion.getNumero()),
				cb.equal(root.join("sustantivoBase").get("genero"), adjetivoFlexion.getGenero()),
				cb.isNotNull(root.get("proximaRevision"))
		);

		return randomSelector.selectRandom(sustantivoFlexionRepo, spec)
				.orElseThrow(() -> new NoSuchElementException(
						String.format("No hay sustantivos inicializados que coincidan con caso=%s, genero=%s, numero=%s",
								adjetivoFlexion.getCaso(), adjetivoFlexion.getGenero(), adjetivoFlexion.getNumero())));
	}

	/**
	 * Devuelve un sustantivo aleatorio apto para actuar como núcleo de una relación
	 * nominal (caso genitivo) en función de la animacidad del sustantivo dependiente.
	 * <ul>
	 *   <li>Si el sustantivo es {@link Animacidad#INANIMADO}, el resultado debe ser
	 *       {@link CabezaRelacional#SI}.</li>
	 *   <li>En cualquier otro caso se devuelve cualquier sustantivo en nominativo.</li>
	 * </ul>
	 *
	 * @param sus sustantivo dependiente (en genitivo)
	 * @return sustantivo aleatorio que cumple la condición
	 * @throws NoSuchElementException si no hay candidatos
	 */
	public SustantivoFlexion getSustantivoParaGenitivo(SustantivoFlexion sus) {
		Specification<SustantivoFlexion> spec;

		if (sus.getSustantivoBase().getAnimacidad() == Animacidad.INANIMADO) {
			spec = (root, query, cb) -> cb.and(
					cb.equal(root.get("caso"), Caso.NOMINATIVO),
					cb.equal(root.join("sustantivoBase").get("cabezaRelacional"), CabezaRelacional.SI)
			);
		} else {
			spec = (root, query, cb) -> cb.equal(root.get("caso"), Caso.NOMINATIVO);
		}

		return randomSelector.selectRandom(sustantivoFlexionRepo, spec)
				.orElseThrow(() -> new NoSuchElementException("No hay sustantivos activos disponibles para el genitivo"));
	}
}

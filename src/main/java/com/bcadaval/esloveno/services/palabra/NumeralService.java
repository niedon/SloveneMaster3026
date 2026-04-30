package com.bcadaval.esloveno.services.palabra;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.repo.NumeralFlexionRepo;
import com.bcadaval.esloveno.services.RandomEntitySelector;
import jakarta.persistence.criteria.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.repo.NumeralRepo;

/**
 * Servicio para gestionar numerales y sus flexiones.
 * <p>
 * Proporciona métodos semánticos para obtener numerales según criterios
 * gramaticales, delegando la lógica de filtrado a Specifications JPA
 * y usando {@link RandomEntitySelector} para la aleatoriedad eficiente.
 */
@SuppressWarnings("unused")
@Service
public class NumeralService {

	@Autowired
	NumeralRepo numeralRepo;

	@Autowired
	NumeralFlexionRepo numeralFlexionRepo;

	@Autowired
	private RandomEntitySelector randomSelector;

	/**
	 * Devuelve un numeral que coincida con el adjetivo dado en número, caso y género.
	 * <p>
	 * <strong>Uso exclusivo de generadores.</strong> No filtra por SRS.
	 *
	 * @param adjetivoFlexion adjetivo del que tomar número, caso y género
	 * @return numeral que coincide, o null si no se encuentra
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
	 * <p>
	 * <strong>Uso exclusivo de generadores.</strong> No filtra por SRS.
	 *
	 * @param sustantivoFlexion sustantivo del que tomar número, caso y género
	 * @return numeral que coincide, o null si no se encuentra
	 */
	public NumeralFlexion getNumeral(SustantivoFlexion sustantivoFlexion) {
		return getNumeral(
				sustantivoFlexion.getNumero(),
				sustantivoFlexion.getCaso(),
				sustantivoFlexion.getSustantivoBase().getGenero()
		);
	}

	/**
	 * Devuelve un numeral aleatorio que coincida con número, caso y género,
	 * aplicando las reglas de principal según el número:
	 *
	 * @param numero número gramatical requerido
	 * @param caso   caso gramatical requerido
	 * @param genero género gramatical requerido (puede ser null)
	 * @return numeral que coincide, o null si no se encuentra
	 */
	public NumeralFlexion getNumeral(Numero numero, Caso caso, Genero genero) {
		Specification<NumeralFlexion> spec = Specification
				.where(conCaso(caso))
				.and(conNumero(numero))
				.and(conGeneroOpcional(genero))
				.and(filtroPrincipalPorNumero(numero));

		return randomSelector.selectRandom(numeralFlexionRepo, spec).orElse(null);
	}

	/**
	 * Devuelve un numeral con cantidad ≥ 5 que coincida con el sustantivo dado.
	 * <p>
	 * <strong>Uso exclusivo de generadores.</strong> No filtra por SRS.
	 *
	 * @param sustantivoFlexion sustantivo del que tomar género, número y caso
	 * @return numeral con cantidad ≥ 5, o null si no se encuentra
	 */
	public NumeralFlexion getNumeralGrande(SustantivoFlexion sustantivoFlexion) {
		Specification<NumeralFlexion> spec = Specification
				.where(conCaso(sustantivoFlexion.getCaso()))
				.and(conNumero(sustantivoFlexion.getNumero()))
				.and(conGeneroOpcional(sustantivoFlexion.getSustantivoBase().getGenero()))
				.and(conCantidadMinima(5));

		return randomSelector.selectRandom(numeralFlexionRepo, spec).orElse(null);
	}

	// ============================================
	// Specifications privadas reutilizables
	// ============================================

	private static Specification<NumeralFlexion> conCaso(Caso caso) {
		return (root, query, cb) -> cb.equal(root.get("caso"), caso);
	}

	private static Specification<NumeralFlexion> conNumero(Numero numero) {
		return (root, query, cb) -> cb.equal(root.get("numero"), numero);
	}

	private static Specification<NumeralFlexion> conGeneroOpcional(Genero genero) {
		if (genero == null) {
			return Specification.where(null);
		}
		return (root, query, cb) -> cb.equal(root.get("genero"), genero);
	}

	/**
	 * Filtra numerales por principal según el número gramatical:
	 * SINGULAR → "en", DUAL → "dva", PLURAL → ni "en" ni "dva".
	 */
	private static Specification<NumeralFlexion> filtroPrincipalPorNumero(Numero numero) {
		return (root, query, cb) -> switch (numero) {
			case SINGULAR -> cb.equal(root.get("principal"), "en");
			case DUAL -> cb.equal(root.get("principal"), "dva");
			case PLURAL -> cb.and(
					cb.notEqual(root.get("principal"), "en"),
					cb.notEqual(root.get("principal"), "dva")
			);
		};
	}

	/**
	 * Filtra numerales cuya palabra base tiene cantidad >= al valor dado.
	 */
	@SuppressWarnings("SameParameterValue")
	private static Specification<NumeralFlexion> conCantidadMinima(int minimo) {
		return (root, query, cb) ->
				cb.greaterThanOrEqualTo(
						root.join("numeralBase", JoinType.INNER).get("cantidad"),
						minimo);
	}

	/**
	 * Filtra numerales cuya palabra base tiene cantidad >= al valor dado.
	 */
	private static Specification<NumeralFlexion> conCantidadMaxima(int maximo) {
		return (root, query, cb) ->
				cb.lessThanOrEqualTo(
						root.join("numeralBase", JoinType.INNER).get("cantidad"),
						maximo);
	}
}


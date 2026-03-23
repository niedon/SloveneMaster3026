package com.bcadaval.esloveno.services.palabra;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.PronombreFlexionRepo;
import com.bcadaval.esloveno.services.RandomEntitySelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar pronombres y sus flexiones.
 * <p>
 * Proporciona métodos semánticos para obtener pronombres según criterios
 * gramaticales, usando Specifications JPA y {@link RandomEntitySelector}.
 */
@Service
public class PronombreService {

	@Autowired
	PronombreFlexionRepo pronombreFlexionRepo;

	@Autowired
	private RandomEntitySelector randomSelector;

	/**
	 * Devuelve un pronombre nominativo no clítico que coincide con la persona y número del verbo dado.
	 * <p>
	 * Toda la lógica de filtrado se ejecuta en BD: persona, número, caso = NOMINATIVO,
	 * y clítico = null o false.
	 *
	 * @param verboFlexion verbo del que tomar persona y número
	 * @return pronombre que coincide, o null si no se encuentra
	 */
	public PronombreFlexion getPronombre(VerboFlexion verboFlexion) {
		if (verboFlexion == null || verboFlexion.getPersona() == null || verboFlexion.getNumero() == null) {
			return null;
		}

		Specification<PronombreFlexion> spec = (root, query, cb) -> cb.and(
				cb.equal(root.get("persona"), verboFlexion.getPersona()),
				cb.equal(root.get("numero"), verboFlexion.getNumero()),
				cb.equal(root.get("caso"), Caso.NOMINATIVO),
				cb.or(
						cb.isNull(root.get("clitico")),
						cb.equal(root.get("clitico"), false)
				)
		);

		return randomSelector.selectRandom(pronombreFlexionRepo, spec).orElse(null);
	}

	/**
	 * Obtiene un pronombre aleatorio que cumpla con los criterios especificados.
	 * 
	 * @param numero número gramatical (opcional)
	 * @param genero género gramatical (opcional)
	 * @param caso caso gramatical (opcional)
	 * @param tipo tipo de pronombre (opcional)
	 * @return PronombreFlexion que cumple los criterios, o null si no se encuentra
	 */
	public PronombreFlexion getAnyPronombre(com.bcadaval.esloveno.beans.enums.Numero numero, 
											com.bcadaval.esloveno.beans.enums.Genero genero, 
											Caso caso, 
											com.bcadaval.esloveno.beans.enums.TipoPronombre tipo) {
		Specification<PronombreFlexion> spec = (root, query, cb) -> {
			jakarta.persistence.criteria.Predicate p = cb.conjunction();
			if (numero != null) {
				p = cb.and(p, cb.equal(root.get("numero"), numero));
			}
			if (genero != null) {
				// Asumimos que si el pronombre tiene género específico debe coincidir,
				// o si es null (común) también vale? 
				// Por seguridad, buscamos coincidencia exacta o género nulo si eso aplica en tu modelo.
				// Simplificación: coincidencia exacta.
				p = cb.and(p, cb.equal(root.get("genero"), genero));
			}
			if (caso != null) {
				p = cb.and(p, cb.equal(root.get("caso"), caso));
			}
			if (tipo != null) {
				p = cb.and(p, cb.equal(root.get("pronombreBase").get("tipoPronombre"), tipo));
			}
			return p;
		};
		
		return randomSelector.selectRandom(pronombreFlexionRepo, spec).orElse(null);
	}
}

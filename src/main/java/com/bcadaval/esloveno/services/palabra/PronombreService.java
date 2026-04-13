package com.bcadaval.esloveno.services.palabra;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.TipoPronombre;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.PronombreFlexionRepo;
import com.bcadaval.esloveno.services.RandomEntitySelector;
import jakarta.persistence.criteria.Predicate;
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

	public PronombreFlexion getAnyPronombre(VerboFlexion verboFlexion) {
		Specification<PronombreFlexion> spec = (root, query, cb) -> {
			Predicate p = cb.conjunction();

			p = cb.and(p, cb.equal(root.get("caso"), Caso.NOMINATIVO));

			p = cb.and(p, cb.equal(root.get("pronombreBase").get("tipoPronombre"), TipoPronombre.PERSONAL));

			p = cb.and(p, cb.equal(root.get("numero"), verboFlexion.getNumero()));

			if(verboFlexion.getPersona()!=null) {
				p = cb.and(p, cb.equal(root.get("persona"), verboFlexion.getPersona()));
			}

			if (verboFlexion.getGenero() != null) {
				p = cb.and(p, cb.or(
						cb.isNull(root.get("genero")),
						cb.equal(root.get("genero"), verboFlexion.getGenero())
				));
			}

			//TODO ver cómo afecta clitico

			return p;
		};
		return randomSelector.selectRandom(pronombreFlexionRepo, spec).orElse(null);
	}

}

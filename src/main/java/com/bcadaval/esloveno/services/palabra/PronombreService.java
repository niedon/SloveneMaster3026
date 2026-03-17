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
}

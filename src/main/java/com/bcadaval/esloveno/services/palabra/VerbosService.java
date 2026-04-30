package com.bcadaval.esloveno.services.palabra;

import java.util.List;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.repo.VerboRepo;
import com.bcadaval.esloveno.services.RandomEntitySelector;
import jakarta.persistence.criteria.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar verbos y sus flexiones.
 * <p>
 * Proporciona métodos semánticos de alto nivel para obtener verbos
 * según criterios gramaticales, delegando toda la lógica de filtrado
 * a Specifications JPA ejecutadas en base de datos.
 */
@SuppressWarnings({"SameParameterValue", "unused"})
@Service
public class VerbosService {

	@Autowired
	private VerboRepo verboRepo;

	@Autowired
	private VerboFlexionRepo verboFlexionRepo;

	@Autowired
	private RandomEntitySelector randomSelector;

	/**
	 * Obtiene un verbo transitivo en presente de forma aleatoria.
	 * Excluye verbos negativos. No filtra por lista de ignorados.
	 *
	 * @return flexión de verbo transitivo en presente, o null si no hay candidatos
	 */
	public VerboFlexion getVerboTransitivoPresenteAleatorio() {
		return getVerboTransitivoPresenteAleatorio(List.of());
	}

	/**
	 * Obtiene un verbo transitivo en presente de forma aleatoria,
	 * excluyendo los verbos cuyo principal esté en la lista de ignorados.
	 * <p>
	 * Toda la lógica de filtrado se ejecuta en BD mediante Specification:
	 * forma verbal = PRESENT, negativo = false, transitividad = TRANSITIVO,
	 * y principal NOT IN (ignorados).
	 *
	 * @param verbosIgnorados lista de principales a excluir
	 * @return flexión de verbo transitivo en presente, o null si no hay candidatos
	 */
	public VerboFlexion getVerboTransitivoPresenteAleatorio(List<String> verbosIgnorados) {
		Specification<VerboFlexion> spec = Specification
				.where(conFormaVerbal(FormaVerbal.PRESENT))
				.and(conNegativo(false))
				.and(conTransitividadBase(Transitividad.TRANSITIVO));

		if (verbosIgnorados != null && !verbosIgnorados.isEmpty()) {
			spec = spec.and(principalBaseNotIn(verbosIgnorados));
		}

		return randomSelector.selectRandom(verboFlexionRepo, spec).orElse(null);
	}

	/**
	 * Obtiene una forma verbal específica (ej. para auxiliares).
	 *
	 * @param principal palabra principal (infinitivo)
	 * @param forma forma verbal
	 * @param persona persona (opcional)
	 * @param numero número (opcional)
	 * @param negativo si es negativo (opcional)
	 * @return VerboFlexion encontrado o null
	 */
	public VerboFlexion getVerboAuxiliar(String principal, FormaVerbal forma, Persona persona, Numero numero, Boolean negativo) {
		Specification<VerboFlexion> spec = (root, query, cb) -> {
			jakarta.persistence.criteria.Predicate p = cb.conjunction();
			if (principal != null) {
				p = cb.and(p, cb.equal(root.get("verboBase").get("principal"), principal));
			}
			if (forma != null) {
				p = cb.and(p, cb.equal(root.get("formaVerbal"), forma));
			}
			if (persona != null) {
				p = cb.and(p, cb.equal(root.get("persona"), persona));
			}
			if (numero != null) {
				p = cb.and(p, cb.equal(root.get("numero"), numero));
			}
			if (negativo != null) {
				p = cb.and(p, cb.equal(root.get("negativo"), negativo));
			}
			return p;
		};

		return randomSelector.selectRandom(verboFlexionRepo, spec).orElse(null);
	}

	// ============================================
	// Specifications privadas reutilizables
	// ============================================

	private static Specification<VerboFlexion> conFormaVerbal(FormaVerbal forma) {
		return (root, query, cb) -> cb.equal(root.get("formaVerbal"), forma);
	}

	private static Specification<VerboFlexion> conNegativo(boolean negativo) {
		return (root, query, cb) -> cb.equal(root.get("negativo"), negativo);
	}

	private static Specification<VerboFlexion> conPersona(Persona persona) {
		return (root, query, cb) -> cb.equal(root.get("persona"), persona);
	}

	private static Specification<VerboFlexion> conNumero(Numero numero) {
		return (root, query, cb) -> cb.equal(root.get("numero"), numero);
	}

	private static Specification<VerboFlexion> conTransitividadBase(Transitividad transitividad) {
		return (root, query, cb) -> cb.equal(
				root.join("verboBase", JoinType.INNER).get("transitividad"),
				transitividad);
	}

	private static Specification<VerboFlexion> conTransitividadBaseIn(Transitividad... transitividades) {
		return (root, query, cb) ->
				root.join("verboBase", JoinType.INNER).get("transitividad").in((Object[]) transitividades);
	}

	private static Specification<VerboFlexion> principalBaseNotIn(List<String> principales) {
		return (root, query, cb) ->
				root.join("verboBase", JoinType.INNER).get("principal").in(principales).not();
	}
}

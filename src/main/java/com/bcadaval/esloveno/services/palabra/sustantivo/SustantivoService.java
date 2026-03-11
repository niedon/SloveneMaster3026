package com.bcadaval.esloveno.services.palabra.sustantivo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

import com.bcadaval.esloveno.beans.enums.Animacidad;
import com.bcadaval.esloveno.beans.enums.CabezaRelacional;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;

@Service
public class SustantivoService {
	
	@Autowired
	private SustantivoFlexionRepo sustantivoFlexionRepo;

	public SustantivoFlexion getSustantivoAleatorio() throws NoSuchElementException {
		long count = sustantivoFlexionRepo.count();
		if (count == 0) {
			throw new NoSuchElementException("No hay sustantivos disponibles");
		}

		int randomPage = ThreadLocalRandom.current().nextInt(0, (int) Math.ceil((double) count / 100));
		List<SustantivoFlexion> pagina = sustantivoFlexionRepo
				.findAll(PageRequest.of(randomPage, 100))
				.getContent();

		if (pagina.isEmpty()) {
			throw new NoSuchElementException("No hay sustantivos disponibles");
		}

		return pagina.get(ThreadLocalRandom.current().nextInt(pagina.size()));
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
		// Buscar sustantivos que coincidan en caso, número y género (filtrando en BD)
		List<SustantivoFlexion> coincidentes = sustantivoFlexionRepo.findByCasoAndNumeroAndGenero(
				adjetivoFlexion.getCaso(),
				adjetivoFlexion.getNumero(),
				adjetivoFlexion.getGenero()
		);

		if (coincidentes.isEmpty()) {
			throw new NoSuchElementException(
					String.format("No hay sustantivos inicializados que coincidan con caso=%s, genero=%s, numero=%s",
							adjetivoFlexion.getCaso(), adjetivoFlexion.getGenero(), adjetivoFlexion.getNumero()));
		}

		// Devolver uno aleatorio
		return coincidentes.get(ThreadLocalRandom.current().nextInt(coincidentes.size()));
	}

	/**
	 * Devuelve un {@link SustantivoFlexion} aleatorio apto para actuar como núcleo
	 * de una relación nominal (caso genitivo) en función de la animacidad del sustantivo
	 * dependiente recibido.
	 * <ul>
	 *   <li>Si {@code sus} es {@link Animacidad#INANIMADO}, el sustantivo devuelto debe
	 *       ser {@link CabezaRelacional#SI} (cabeza relacional).</li>
	 *   <li>En cualquier otro caso se devuelve cualquier sustantivo aleatorio.</li>
	 * </ul>
	 *
	 * @param sus sustantivo dependiente (en genitivo) a partir del cual se determina la condición
	 * @return sustantivo aleatorio que cumple la condición
	 * @throws NoSuchElementException si no hay sustantivos disponibles que cumplan el criterio
	 */
	public SustantivoFlexion getSustantivoParaGenitivo(SustantivoFlexion sus) {
		List<SustantivoFlexion> candidatos;

		if (sus.getSustantivoBase().getAnimacidad() == Animacidad.INANIMADO) {
			candidatos = sustantivoFlexionRepo.findByCabezaRelacional(Caso.NOMINATIVO, CabezaRelacional.SI);
		} else {
			candidatos = sustantivoFlexionRepo.findByCaso(Caso.NOMINATIVO);
		}

		if (candidatos.isEmpty()) {
			throw new NoSuchElementException("No hay sustantivos activos disponibles para el genitivo");
		}

		return candidatos.get(ThreadLocalRandom.current().nextInt(candidatos.size()));
	}
}

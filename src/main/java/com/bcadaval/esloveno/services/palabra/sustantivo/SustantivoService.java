package com.bcadaval.esloveno.services.palabra.sustantivo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoRepo;

@Service
public class SustantivoService {
	
	@Autowired
	private SustantivoRepo sustantivoRepo;
	
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
	 *
	 * @param adjetivoFlexion Adjetivo con el que debe concordar el sustantivo
	 * @return SustantivoFlexion que concuerda con el adjetivo
	 * @throws NoSuchElementException si no se encuentra ningún sustantivo que concuerde
	 */
	public SustantivoFlexion getSustantivo(AdjetivoFlexion adjetivoFlexion) throws NoSuchElementException {
		// Buscar sustantivos que coincidan en caso y número
		SustantivoFlexion ejemplo = SustantivoFlexion.builder()
				.caso(adjetivoFlexion.getCaso())
				.numero(adjetivoFlexion.getNumero())
				.build();

		List<SustantivoFlexion> candidatos = sustantivoFlexionRepo.findAll(Example.of(ejemplo));

		// Filtrar por género (el género está en el sustantivo base)
		List<SustantivoFlexion> coincidentes = candidatos.stream()
				.filter(sf -> sf.getSustantivoBase() != null)
				.filter(sf -> sf.getSustantivoBase().getGenero() == adjetivoFlexion.getGenero())
				.toList();

		if (coincidentes.isEmpty()) {
			throw new NoSuchElementException(
					String.format("No hay sustantivos que coincidan con caso=%s, genero=%s, numero=%s",
							adjetivoFlexion.getCaso(), adjetivoFlexion.getGenero(), adjetivoFlexion.getNumero()));
		}

		// Devolver uno aleatorio
		return coincidentes.get(ThreadLocalRandom.current().nextInt(coincidentes.size()));
	}
}

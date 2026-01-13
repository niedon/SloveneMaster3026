package com.bcadaval.esloveno.services.palabra.sustantivo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Numero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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
	/*
	public SustantivoFlexion getSustantivoPorEstudiar() throws NoSuchElementException {
		
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAll()
                .withIgnorePaths("tiempoUltimaVezEstudio")
                .withIncludeNullValues();
		
		Example<SustantivoFlexion> example = Example.of(new SustantivoFlexion(), exampleMatcher);
		List<SustantivoFlexion> candidatos = sustantivoFlexionRepo
				.findAllByTiempoUltimaVezEstudioIsNull(PageRequest.of(0, 100))
				.getContent();

		if (candidatos.isEmpty()) {
			throw new NoSuchElementException("No hay sustantivos por estudiar disponibles");
		}

		return candidatos.get(ThreadLocalRandom.current().nextInt(candidatos.size()));

	}
*/
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

	/** Obtiene un {@link SustantivoFlexion sustantivo} aleatorio con la flexión en
	 * {@link com.bcadaval.esloveno.beans.enums.Caso acusativo} y el {@link Numero número} indicado, y con el
	 * campo tiempoUltimaVezEstudio nulo.
	 *
	 * @param numero Número gramatical del sustantivo a obtener
	 * @return SustantivoFlexion en acusativo y el número indicado
	 */
	/*
	public SustantivoFlexion getComplementoDirecto(Numero numero) throws NoSuchElementException {
		// Crear ejemplo con los criterios de búsqueda
		SustantivoFlexion example = SustantivoFlexion.builder()
				.caso(Caso.ACUSATIVO)
				.numero(numero)
				.build();

		// Configurar matcher para que solo busque por caso y numero, ignorando todos los demás campos
		ExampleMatcher exampleMatcher = ExampleMatcher.matching()
				.withIgnorePaths("id", "principal", "flexion", "acentuado",
						"pronunciacionIpa", "pronunciacionSampa",
						"tiempoUltimaVezEstudio", "tiempoProximaVezEstudio", "sustantivoBase")
				.withIgnoreNullValues();

		Example<SustantivoFlexion> exampleQuery = Example.of(example, exampleMatcher);

		// Buscar candidatos que cumplan los criterios
		List<SustantivoFlexion> candidatos = sustantivoFlexionRepo
				.findAll(exampleQuery, PageRequest.of(0, 100))
				.getContent();

		// Filtrar por tiempoUltimaVezEstudio nulo
		candidatos = candidatos.stream()
				.filter(s -> s.getTiempoUltimaVezEstudio() == null)
				.toList();

		if (candidatos.isEmpty()) {
			throw new NoSuchElementException(
					String.format("No hay sustantivos en acusativo y número %s por estudiar disponibles", numero));
		}

		return candidatos.get(ThreadLocalRandom.current().nextInt(candidatos.size()));
	}
*/
}

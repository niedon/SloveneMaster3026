package com.bcadaval.esloveno.services.palabra.sustantivo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
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


}

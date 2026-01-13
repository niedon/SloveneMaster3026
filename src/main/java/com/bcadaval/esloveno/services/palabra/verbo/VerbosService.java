package com.bcadaval.esloveno.services.palabra.verbo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.beans.palabra.Verbo;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.repo.VerboRepo;

@Service
public class VerbosService {

	@Autowired
	private VerboRepo verboRepo;
	
	@Autowired
	private VerboFlexionRepo verboFlexionRepo;

	public List<Verbo> findAll() {
		return verboRepo.findAll();
	}
	
	public Verbo findById(String principal) throws VerboNotFoundException {
		return verboRepo.findById(principal).orElseThrow(VerboNotFoundException::new);
	}
	
	public boolean verbHasConjugations(String verb) {
		return verboRepo.findById(verb).isPresent() && verboFlexionRepo.count(Example.of(VerboFlexion.builder().principal(verb).build())) > 8;
	}
	
	public List<VerboFlexion> saveConjugations(List<VerboFlexion> conjugations) throws VerboNotFoundException {
		verboRepo.findById(conjugations.getFirst().getPrincipal()).orElseThrow(VerboNotFoundException::new);
		return verboFlexionRepo.saveAll(conjugations);
	}
}

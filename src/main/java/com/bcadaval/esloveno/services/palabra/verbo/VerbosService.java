package com.bcadaval.esloveno.services.palabra.verbo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
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
	
	public Verbo findById(String sloleksId) throws VerboNotFoundException {
		return verboRepo.findById(sloleksId).orElseThrow(VerboNotFoundException::new);
	}
	
	public boolean verbHasConjugations(String sloleksId) {
		return verboRepo.findById(sloleksId).isPresent() && verboFlexionRepo.count(Example.of(VerboFlexion.builder().sloleksId(sloleksId).build())) > 8;
	}
	
	public List<VerboFlexion> saveConjugations(List<VerboFlexion> conjugations) throws VerboNotFoundException {
		verboRepo.findById(conjugations.getFirst().getSloleksId()).orElseThrow(VerboNotFoundException::new);
		return verboFlexionRepo.saveAll(conjugations);
	}
}

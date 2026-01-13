package com.bcadaval.esloveno.beans.palabra;

import java.util.List;

import com.bcadaval.esloveno.beans.base.Palabra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Accessors(chain = true)
/**
 * Representa un adjetivo en esloveno.
 * Contiene la forma principal del adjetivo, su acentuación,
 * identificadores en Sloleks, significado en español y una lista de sus flexiones.
 */
public class Adjetivo implements Palabra<AdjetivoFlexion> {
	
	@Id
	private String principal;
	
	@Column(nullable = true)
	private String acentuado;

	private String sloleksId;
	private String sloleksKey;
	
	private String significado;
	
	@Transient
	private List<AdjetivoFlexion> listaFlexiones;

}

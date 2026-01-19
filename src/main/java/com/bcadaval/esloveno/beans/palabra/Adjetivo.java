package com.bcadaval.esloveno.beans.palabra;

import java.util.List;

import com.bcadaval.esloveno.beans.base.Palabra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Representa un adjetivo en esloveno.
 * Contiene la forma principal del adjetivo, su acentuación,
 * identificadores en Sloleks, significado en español y una lista de sus flexiones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Accessors(chain = true)
@ToString
public class Adjetivo implements Palabra<AdjetivoFlexion> {
	
	@Id
	private String principal;
	
	@Column
	private String acentuado;

	private String sloleksId;
	private String sloleksKey;
	
	private String significado;
	
	@Transient
	private List<AdjetivoFlexion> listaFlexiones;

}

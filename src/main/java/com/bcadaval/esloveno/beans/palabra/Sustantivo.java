package com.bcadaval.esloveno.beans.palabra;

import java.util.List;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.enums.Animacidad;
import com.bcadaval.esloveno.beans.enums.CabezaRelacional;
import com.bcadaval.esloveno.beans.enums.ClaseSemantica;
import com.bcadaval.esloveno.beans.enums.Contabilidad;
import com.bcadaval.esloveno.beans.enums.Genero;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Representa un sustantivo en esloveno.
 * Contiene la forma principal del sustantivo, su acentuación,
 * género, animacidad, contabilidad, clase semántica, cabeza relacional,
 * identificadores en Sloleks, significado en español y una lista de sus flexiones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Accessors(chain = true)
@ToString
public class Sustantivo implements Palabra<SustantivoFlexion> {
	
	@Id
	private String sloleksId;

	private String principal;
	
	private Genero genero;

	/**
	 * Animacidad del sustantivo. Reemplaza al antiguo campo {@code Boolean animado}.
	 * Nullable: se asigna manualmente en la pantalla de completar palabras.
	 */
	private Animacidad animacidad;

	/**
	 * Contabilidad del sustantivo (contable/incontable).
	 * Nullable: se asigna manualmente en la pantalla de completar palabras.
	 */
	private Contabilidad contabilidad;

	/**
	 * Clase semántica del sustantivo (humano, animal, objeto, lugar, sustancia, abstracto).
	 * Nullable: se asigna manualmente en la pantalla de completar palabras.
	 */
	private ClaseSemantica claseSemantica;

	/**
	 * Indica si el sustantivo es cabeza relacional.
	 * Nullable: se asigna manualmente en la pantalla de completar palabras.
	 */
	private CabezaRelacional cabezaRelacional;

	private String sloleksKey;
	
	private String significado;
	
	@Transient
	private List<SustantivoFlexion> listaFlexiones;

}

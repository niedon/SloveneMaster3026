package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.enums.Animacidad;
import com.bcadaval.esloveno.beans.enums.ClaseSemantica;
import com.bcadaval.esloveno.beans.enums.Contabilidad;
import com.bcadaval.esloveno.beans.enums.Genero;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * Representa un sustantivo en esloveno.
 * Contiene la forma principal del sustantivo, su acentuación,
 * género, animacidad, contabilidad, clase semántica, identificadores
 * en Sloleks, significado en español y una lista de sus flexiones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Sustantivo extends Palabra<SustantivoFlexion> {
	
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

}

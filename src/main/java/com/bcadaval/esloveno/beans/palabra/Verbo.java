package com.bcadaval.esloveno.beans.palabra;

import java.util.List;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.enums.Aspecto;
import com.bcadaval.esloveno.beans.enums.RequiereObjetoAnimado;
import com.bcadaval.esloveno.beans.enums.RequiereSujetoAnimado;
import com.bcadaval.esloveno.beans.enums.Transitividad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Representa un verbo en esloveno.
 * Contiene la forma principal del verbo, su acentuación,
 * transitividad, aspecto, requisitos de animacidad del sujeto y objeto,
 * verbo correspondiente del otro aspecto (puede no tener),
 * identificadores en Sloleks, significado en español y una lista de sus flexiones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Accessors(chain = true)
@ToString
public class Verbo implements Palabra<VerboFlexion> {

	@Id
	private String sloleksId;

	private String principal;

	@Column
	private Transitividad transitividad;

	private Aspecto aspecto;
	
	private String verboOtroAspecto;

	/**
	 * Indica si el verbo requiere que su sujeto sea animado.
	 * Nullable: se asigna manualmente en la pantalla de completar palabras.
	 */
	private RequiereSujetoAnimado requiereSujetoAnimado;

	/**
	 * Indica si el verbo requiere que su objeto directo sea animado.
	 * Nullable: se asigna manualmente en la pantalla de completar palabras.
	 */
	private RequiereObjetoAnimado requiereObjetoAnimado;

	private String significado;

	private String sloleksKey;

	@Transient
	private List<VerboFlexion> listaFlexiones;

}

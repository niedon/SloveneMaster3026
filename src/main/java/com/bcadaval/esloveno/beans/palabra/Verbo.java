package com.bcadaval.esloveno.beans.palabra;

import java.util.List;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.enums.Aspecto;
import com.bcadaval.esloveno.beans.enums.Transitividad;

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
 * Representa un verbo en esloveno.
 * Contiene la forma principal del verbo, su acentuación,
 * transitividad, aspecto, verbo correspondiente del otro aspecto (puede no tener),
 * identificadores en Sloleks, significado en español y una lista de sus flexiones.
 */
public class Verbo implements Palabra<VerboFlexion> {

	@Id
	private String principal;

	@Column(nullable = true)
	private String acentuado;

	@Column(nullable = true)
	private Transitividad transitividad;

	private Aspecto aspecto;
	
	private String verboOtroAspecto;
	
	private String significado;

	private String sloleksId;
	private String sloleksKey;

	@Transient
	private List<VerboFlexion> listaFlexiones;

}

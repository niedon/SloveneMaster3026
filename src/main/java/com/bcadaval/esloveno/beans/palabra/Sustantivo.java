package com.bcadaval.esloveno.beans.palabra;

import java.util.List;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.enums.Genero;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Representa un sustantivo en esloveno.
 * Contiene la forma principal del sustantivo, su acentuación,
 * género, animacidad, identificadores en Sloleks, significado en español y una lista de sus flexiones.
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
	
	private Boolean animado;
	
	private String sloleksKey;
	
	private String significado;
	
	@Transient
	private List<SustantivoFlexion> listaFlexiones;

}

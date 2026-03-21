package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * Representa un adjetivo en esloveno.
 * Contiene la forma principal del adjetivo, su acentuación,
 * identificadores en Sloleks, significado en español y una lista de sus flexiones.
 */
@Data
@NoArgsConstructor
//@AllArgsConstructor
@SuperBuilder
@Entity
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Adjetivo extends Palabra<AdjetivoFlexion> {
	
    // No fields unique to Adjetivo for now, but extending base class
    // allows for future expansion and standard type hierarchy

}

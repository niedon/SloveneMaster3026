package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.enums.TipoPronombre;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Pronombre extends Palabra<PronombreFlexion> {

	@Enumerated(EnumType.STRING)
	private TipoPronombre tipoPronombre;

}

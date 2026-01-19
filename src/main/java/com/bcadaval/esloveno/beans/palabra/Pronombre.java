package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.enums.TipoPronombre;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString
public class Pronombre implements Palabra<PronombreFlexion> {

	@Id
	private String principal;

	@Column
	private String acentuado;

	@Enumerated(EnumType.STRING)
	private TipoPronombre tipoPronombre;

	private String sloleksId;
	private String sloleksKey;

	private String significado;

	@Transient
	private List<PronombreFlexion> listaFlexiones;

}

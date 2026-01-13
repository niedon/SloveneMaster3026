package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.enums.TipoPronombre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Pronombre implements Palabra<PronombreFlexion> {

	@Id
	private String principal;

	@Column(nullable = true)
	private String acentuado;

	@Enumerated(EnumType.STRING)
	private TipoPronombre tipoPronombre;

	private String sloleksId;
	private String sloleksKey;

	private String significado;

	@Transient
	private List<PronombreFlexion> listaFlexiones;

}

package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Accessors(chain = true)
@ToString
public class Numeral implements Palabra<NumeralFlexion> {

    @Id
    private String sloleksId;

    private String principal;

    private String sloleksKey;

    private String significado;

    @Transient
    private List<NumeralFlexion> listaFlexiones;
}

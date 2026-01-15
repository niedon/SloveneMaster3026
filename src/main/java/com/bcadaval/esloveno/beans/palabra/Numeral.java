package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Accessors(chain = true)
public class Numeral implements Palabra<NumeralFlexion> {

    @Id
    private String principal;

    @Column
    private String acentuado;

    private String sloleksId;
    private String sloleksKey;

    private String significado;

    @Transient
    private List<NumeralFlexion> listaFlexiones;
}

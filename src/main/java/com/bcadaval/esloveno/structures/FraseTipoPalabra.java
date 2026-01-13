package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.Pronombre;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import lombok.Getter;

@Getter
public enum FraseTipoPalabra {
    VERBO_FLEXION(VerboFlexion.class, "v"),
    SUSTANTIVO_FLEXION(SustantivoFlexion.class, "s"),
    ADJETIVO_FLEXION(AdjetivoFlexion.class, "a"),
    PRONOMBRE(Pronombre.class, "p"),
    NUMERO(Numero.class, "n");

    private final Class<?> clazz;
    private final String codigo;

    FraseTipoPalabra(Class<?> clazz, String codigo) {
        this.clazz = clazz;
        this.codigo = codigo;
    }

    /**
     * Obtiene el tipo de palabra a partir de un objeto
     * @param objeto Objeto del cual determinar el tipo
     * @return FraseTipoPalabra correspondiente o null si no se encuentra
     */
    public static FraseTipoPalabra fromObject(Object objeto) {
        if (objeto == null) {
            return null;
        }
        for (FraseTipoPalabra tipo : values()) {
            if (tipo.clazz.isInstance(objeto)) {
                return tipo;
            }
        }
        return null;
    }

    /**
     * Obtiene el tipo de palabra a partir del código
     * @param codigo Código de una letra ('v', 's', 'a', 'p', 'n')
     * @return FraseTipoPalabra correspondiente o null si no se encuentra
     */
    public static FraseTipoPalabra fromCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (FraseTipoPalabra tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return tipo;
            }
        }
        return null;
    }
}

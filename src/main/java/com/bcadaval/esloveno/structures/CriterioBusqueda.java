package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import lombok.Getter;

/**
 * Criterio de búsqueda unificado para un elemento de frase.
 * Encapsula un CriterioGramatical para filtrado en memoria.
 * <p>
 * Uso con builder fluido:
 * <pre>
 * CriterioBusqueda.de(VerboFlexion.class)
 *     .con(CaracteristicaGramatical.FORMA_VERBAL, FormaVerbal.PRESENT)
 *     .build()
 * </pre>
 *
 * @param <T> Tipo de PalabraFlexion que busca
 */
@Getter
public class CriterioBusqueda<T extends PalabraFlexion<?>> {

    /**
     * Clase del tipo de flexión que busca este criterio.
     */
    private final Class<T> tipoFlexion;

    /**
     * Criterio gramatical para filtrado en memoria.
     */
    private final CriterioGramatical criterioGramatical;

    private CriterioBusqueda(Class<T> tipoFlexion, CriterioGramatical criterioGramatical) {
        this.tipoFlexion = tipoFlexion;
        this.criterioGramatical = criterioGramatical;
    }

    /**
     * Inicia un builder fluido para crear un CriterioBusqueda.
     *
     * @param tipoFlexion Clase del tipo de flexión
     * @return Builder configurado
     */
    public static <T extends PalabraFlexion<?>> Builder<T> de(Class<T> tipoFlexion) {
        return new Builder<>(tipoFlexion);
    }

    /**
     * Verifica si una PalabraFlexion cumple este criterio.
     * Primero comprueba el tipo, luego aplica el criterio gramatical.
     *
     * @param palabra Palabra a verificar
     * @return true si cumple el criterio
     */
    public boolean cumple(PalabraFlexion<?> palabra) {
        if (palabra == null) return false;
        if (!tipoFlexion.isInstance(palabra)) return false;
        return criterioGramatical.cumple(palabra);
    }

    /**
     * Builder fluido para CriterioBusqueda
     */
    public static class Builder<T extends PalabraFlexion<?>> {
        private final Class<T> tipoFlexion;
        private final CriterioGramatical.Builder<T> criterioBuilder;

        private Builder(Class<T> tipoFlexion) {
            this.tipoFlexion = tipoFlexion;
            this.criterioBuilder = CriterioGramatical.de(tipoFlexion);
        }

        /**
         * Añade un requisito de característica.
         */
        public Builder<T> con(CaracteristicaGramatical caracteristica, Object valor) {
            this.criterioBuilder.con(caracteristica, valor);
            return this;
        }

        public CriterioBusqueda<T> build() {
            return new CriterioBusqueda<>(tipoFlexion, criterioBuilder.build());
        }
    }
}



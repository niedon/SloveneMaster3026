package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Criterio de filtrado gramatical unificado.
 * Almacena requisitos de características gramaticales y verifica
 * si una PalabraFlexion los cumple usando la función getCaracteristica().
 * <p>
 * Uso:
 * <pre>
 * CriterioGramatical criterio = CriterioGramatical.de(SustantivoFlexion.class)
 *     .con(CaracteristicaGramatical.CASO, Caso.NOMINATIVO)
 *     .build();
 *
 * boolean cumple = criterio.cumple(sustantivoFlexion);
 * </pre>
 */
@Getter
public class CriterioGramatical {

    /**
     * Mapa de requisitos: característica -> valor esperado
     */
    private final Map<CaracteristicaGramatical, Object> requisitos;

    /**
     * Clase del tipo de flexión que busca este criterio.
     */
    private final Class<? extends PalabraFlexion<?>> tipoFlexion;

    private CriterioGramatical(Class<? extends PalabraFlexion<?>> tipoFlexion,
                                Map<CaracteristicaGramatical, Object> requisitos) {
        this.tipoFlexion = tipoFlexion;
        this.requisitos = new EnumMap<>(requisitos);
    }

    /**
     * Verifica si una PalabraFlexion cumple todos los requisitos.
     *
     * @param palabra La palabra a verificar
     * @return true si cumple todos los requisitos
     */
    public boolean cumple(PalabraFlexion<?> palabra) {
        if (palabra == null) return false;
        if (!tipoFlexion.isInstance(palabra)) return false;

        for (var entry : requisitos.entrySet()) {
            Object valorEsperado = entry.getValue();
            Object valorReal = palabra.getCaracteristica(entry.getKey());

            if (!Objects.equals(valorEsperado, valorReal)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Crea un builder para construir un CriterioGramatical.
     */
    public static <T extends PalabraFlexion<?>> Builder<T> de(Class<T> tipoFlexion) {
        return new Builder<>(tipoFlexion);
    }

    /**
     * Builder fluido para CriterioGramatical
     */
    public static class Builder<T extends PalabraFlexion<?>> {
        private final Class<T> tipoFlexion;
        private final Map<CaracteristicaGramatical, Object> requisitos = new EnumMap<>(CaracteristicaGramatical.class);

        private Builder(Class<T> tipoFlexion) {
            this.tipoFlexion = tipoFlexion;
        }

        /**
         * Añade un requisito de característica.
         */
        public Builder<T> con(CaracteristicaGramatical caracteristica, Object valor) {
            this.requisitos.put(caracteristica, valor);
            return this;
        }


        public CriterioGramatical build() {
            return new CriterioGramatical(tipoFlexion, requisitos);
        }
    }
}

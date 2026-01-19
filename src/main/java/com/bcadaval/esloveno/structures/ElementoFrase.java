package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.structures.extractores.EstrategiaExtraccion;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.function.Function;

/**
 * Representa un elemento dentro de una estructura de frase.
 * <p>
 * Un elemento puede ser:
 * - SLOT: tiene CriterioBusqueda, busca palabras en repositorios y participa en SRS
 * - APOYO: tiene generadorObjeto, genera palabras dinámicamente basándose en slots
 * <p>
 * Ambos tipos nunca pueden coexistir (validado en build).
 *
 * @param <T> Tipo de PalabraFlexion que maneja este elemento
 */
@Getter
public class ElementoFrase<T extends PalabraFlexion<?>> {

    /**
     * Nombre identificador del elemento (ej.: "VERBO", "CD", "PRONOMBRE")
     */
    private final String nombre;

    /**
     * Criterio de búsqueda para slots.
     * null si es un elemento de apoyo.
     */
    private final CriterioBusqueda<T> criterioBusqueda;

    /**
     * Generador de objeto para elementos de apoyo.
     * null si es un slot.
     */
    private final Function<EstructuraFrase, T> generadorObjeto;

    /**
     * Slot del que depende este elemento de apoyo.
     * null si es un slot.
     */
    private final ElementoFrase<?> slotDependiente;

    /**
     * Estrategia de extracción (singleton reutilizable).
     * Puede ser null si se usan extractores individuales.
     */
    private final EstrategiaExtraccion estrategiaExtraccion;

    /**
     * Extractores individuales (opcionales, sobreescriben estrategia).
     */
    private final Function<PalabraFlexion<?>, String> extractorDeEspanol;
    private final Function<PalabraFlexion<?>, String> extractorAEsloveno;
    private final Function<PalabraFlexion<?>, String> extractorDeEsloveno;
    private final Function<PalabraFlexion<?>, String> extractorAEspanol;

    // ============================================
    // Estado mutable (asignación de palabra)
    // ============================================

    /**
     * Palabra asignada a este elemento (null si no asignado)
     */
    private T palabraAsignada;

    /**
     * Índice de la palabra en la lista original (solo para slots)
     */
    private Integer indiceEnLista;

    private ElementoFrase(Builder<T> builder) {
        this.nombre = builder.nombre;
        this.criterioBusqueda = builder.criterioBusqueda;
        this.generadorObjeto = builder.generadorObjeto;
        this.slotDependiente = builder.slotDependiente;
        this.estrategiaExtraccion = builder.estrategiaExtraccion;
        this.extractorDeEspanol = builder.extractorDeEspanol;
        this.extractorAEsloveno = builder.extractorAEsloveno;
        this.extractorDeEsloveno = builder.extractorDeEsloveno;
        this.extractorAEspanol = builder.extractorAEspanol;
    }

    /**
     * Indica si este elemento es un slot (busca en repositorios).
     */
    public boolean esSlot() {
        return criterioBusqueda != null;
    }

    /**
     * Indica si este elemento es de apoyo (genera objeto dinámicamente).
     */
    public boolean esApoyo() {
        return generadorObjeto != null;
    }

    /**
     * Verifica si una palabra cumple el criterio del slot.
     * Solo válido para slots.
     *
     * @param palabra Palabra a verificar
     * @return true si el slot está vacío y la palabra cumple el criterio
     */
    public boolean coincide(PalabraFlexion palabra) {
        if (!esSlot()) return false;
        return palabraAsignada == null && criterioBusqueda.cumple(palabra);
    }

    /**
     * Asigna una palabra al elemento.
     *
     * @param palabra Palabra a asignar
     * @param indice Índice en la lista original (puede ser null para apoyos)
     */
    public void asignar(PalabraFlexion palabra, Integer indice) {
        this.palabraAsignada = (T)palabra;
        this.indiceEnLista = indice;
    }

    /**
     * Verifica si el elemento tiene una palabra asignada.
     */
    public boolean estaAsignado() {
        return palabraAsignada != null;
    }

    /**
     * Genera el objeto de apoyo usando el contexto de la frase.
     * Solo válido para elementos de apoyo.
     *
     * @param frase Estructura de frase con slots asignados
     * @return Objeto generado o null si el slot dependiente no está asignado
     */
    public T generarObjeto(EstructuraFrase frase) {
        if (!esApoyo()) return null;
        return generadorObjeto.apply(frase);
    }

    /**
     * Obtiene el texto para la fila 1 según el modo de visualización.
     */
    public String getTextoFila1(ModoVisualizacion modo) {
        if (palabraAsignada == null) return "";
        return modo == ModoVisualizacion.ES_SL
                ? getDeEspanol(palabraAsignada)
                : getDeEsloveno(palabraAsignada);
    }

    /**
     * Obtiene el texto para la fila 2 según el modo de visualización.
     */
    public String getTextoFila2(ModoVisualizacion modo) {
        if (palabraAsignada == null) return "";
        return modo == ModoVisualizacion.ES_SL
                ? getAEsloveno(palabraAsignada)
                : getAEspanol(palabraAsignada);
    }

    // Métodos privados para resolver extractor (individual > estrategia)
    private String getDeEspanol(PalabraFlexion p) {
        if (extractorDeEspanol != null) return extractorDeEspanol.apply(p);
        if (estrategiaExtraccion != null) return estrategiaExtraccion.deEspanol(p);
        return "";
    }

    private String getAEsloveno(PalabraFlexion p) {
        if (extractorAEsloveno != null) return extractorAEsloveno.apply(p);
        if (estrategiaExtraccion != null) return estrategiaExtraccion.aEsloveno(p);
        return "";
    }

    private String getDeEsloveno(PalabraFlexion p) {
        if (extractorDeEsloveno != null) return extractorDeEsloveno.apply(p);
        if (estrategiaExtraccion != null) return estrategiaExtraccion.deEsloveno(p);
        return "";
    }

    private String getAEspanol(PalabraFlexion p) {
        if (extractorAEspanol != null) return extractorAEspanol.apply(p);
        if (estrategiaExtraccion != null) return estrategiaExtraccion.aEspanol(p);
        return "";
    }

    /**
     * Limpia el estado del elemento para reutilización.
     */
    public void limpiar() {
        this.palabraAsignada = null;
        this.indiceEnLista = null;
    }

    // ============================================
    // Builder
    // ============================================

    public static <T extends PalabraFlexion<?>> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T extends PalabraFlexion<?>> {
        private String nombre;
        private CriterioBusqueda<T> criterioBusqueda;
        private Function<EstructuraFrase, T> generadorObjeto;
        private ElementoFrase<?> slotDependiente;
        private EstrategiaExtraccion<T> estrategiaExtraccion;
        private Function<PalabraFlexion<?>, String> extractorDeEspanol;
        private Function<PalabraFlexion<?>, String> extractorAEsloveno;
        private Function<PalabraFlexion<?>, String> extractorDeEsloveno;
        private Function<PalabraFlexion<?>, String> extractorAEspanol;

        public Builder<T> nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        /**
         * Configura este elemento como SLOT con criterio de búsqueda.
         * Incompatible con generadorObjeto.
         */
        public Builder<T> criterio(CriterioBusqueda<T> criterio) {
            this.criterioBusqueda = criterio;
            return this;
        }

        /**
         * Configura este elemento como APOYO con generador de objeto.
         * Incompatible con criterio.
         *
         * @param slotDependiente Slot del que depende este apoyo
         * @param generador Función que genera el objeto a partir del slot
         */
        public Builder<T> generador(ElementoFrase<?> slotDependiente,
                                    Function<PalabraFlexion, T> generador) {
            this.slotDependiente = slotDependiente;
            this.generadorObjeto = frase -> {
                if (slotDependiente == null || !slotDependiente.estaAsignado()) return null;
                return generador.apply(slotDependiente.getPalabraAsignada());
            };
            return this;
        }

        /**
         * Configura este elemento como APOYO con generador de objeto personalizado.
         * Incompatible con criterio.
         * Usar cuando se necesita acceso completo a la frase.
         */
        public Builder<T> generadorCompleto(Function<EstructuraFrase, T> generador) {
            this.generadorObjeto = generador;
            return this;
        }

        /**
         * Estrategia de extracción singleton.
         */
        public Builder<T> extractor(EstrategiaExtraccion estrategia) {
            this.estrategiaExtraccion = estrategia;
            return this;
        }

        // Extractores individuales (sobreescriben estrategia)
        public Builder<T> extractorDeEspanol(Function<PalabraFlexion<?>, String> extractor) {
            this.extractorDeEspanol = extractor;
            return this;
        }

        public Builder<T> extractorAEsloveno(Function<PalabraFlexion<?>, String> extractor) {
            this.extractorAEsloveno = extractor;
            return this;
        }

        public Builder<T> extractorDeEsloveno(Function<PalabraFlexion<?>, String> extractor) {
            this.extractorDeEsloveno = extractor;
            return this;
        }

        public Builder<T> extractorAEspanol(Function<PalabraFlexion<?>, String> extractor) {
            this.extractorAEspanol = extractor;
            return this;
        }

        public ElementoFrase<T> build() {
            // Validaciones
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalStateException("El nombre del elemento es obligatorio");
            }

            boolean tieneSlot = criterioBusqueda != null;
            boolean tieneApoyo = generadorObjeto != null;

            if (tieneSlot && tieneApoyo) {
                throw new IllegalStateException(
                        "ElementoFrase '" + nombre + "' no puede tener criterio Y generador simultáneamente");
            }

            if (!tieneSlot && !tieneApoyo) {
                throw new IllegalStateException(
                        "ElementoFrase '" + nombre + "' debe tener criterio (slot) o generador (apoyo)");
            }

            // Verificar que tenga al menos una forma de extracción
            if (ObjectUtils.allNull(estrategiaExtraccion, extractorDeEspanol, extractorAEsloveno,
                    extractorDeEsloveno, extractorAEspanol)) {
                throw new IllegalStateException(
                        "ElementoFrase '" + nombre + "' debe tener estrategia o extractores");
            }

            return new ElementoFrase<>(this);
        }
    }
}


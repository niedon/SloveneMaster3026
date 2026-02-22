package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.structures.extractores.EstrategiaExtraccion;
import com.bcadaval.esloveno.structures.extractores.ExtraccionNull;
import lombok.Getter;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Representa un elemento dentro de una estructura de frase.
 * <p>
 * Un elemento puede ser de tres tipos según cómo se configure en el Builder:
 * <ul>
 *   <li><b>SLOT</b>: solo tiene {@code criterio}. Busca palabras en repositorios y participa en SRS.
 *       Es obligatorio para que la frase se considere completa.</li>
 *   <li><b>APOYO</b>: solo tiene {@code generador}. Genera palabras dinámicamente (ej: pronombre
 *       a partir del verbo). No participa en SRS.</li>
 *   <li><b>OPCIONAL</b>: tiene {@code criterio} Y {@code generador}. Primero intenta rellenarse
 *       por criterio con las tarjetas SRS disponibles. Si no se rellena, la frase se considera
 *       completa igualmente. Al visualizar, si no fue rellenado por criterio, se usa el generador
 *       como fallback y a efectos del frontend se trata como apoyo (sin botones SRS).</li>
 * </ul>
 *
 * @param <T> Tipo de PalabraFlexion que maneja este elemento
 */
@Getter
public class ElementoFrase<T extends PalabraFlexion<?>> {

    // ============================================
    // Campos inmutables (configuración)
    // ============================================

    /**
     * Nombre identificador del elemento (ej.: "VERBO", "CD", "PRONOMBRE")
     */
    private final String nombre;

    /**
     * Criterio de búsqueda para slots y opcionales.
     * null si es un elemento de apoyo puro.
     */
    private final CriterioBusqueda<T> criterioBusqueda;

    /**
     * Generador de objeto para elementos de apoyo y opcionales.
     * Función que recibe la EstructuraFrase completa y devuelve el objeto generado.
     * null si es un slot puro.
     */
    private final Function<EstructuraFrase, T> generadorObjeto;

    /**
     * Slot del que depende este elemento (apoyo u opcional).
     * null si es un slot puro o si el generador no depende de otro slot.
     */
    private final ElementoFrase<?> slotDependiente;

    /**
     * Estrategia de extracción (singleton reutilizable).
     * Puede ser null si se usan extractores individuales.
     */
    private final EstrategiaExtraccion<T> estrategiaExtraccion;

    /**
     * Extractores individuales (opcionales, sobreescriben estrategia).
     */
    private final Function<T, String> extractorDeEspanol;
    private final Function<T, String> extractorAEsloveno;
    private final Function<T, String> extractorDeEsloveno;
    private final Function<T, String> extractorAEspanol;

    // ============================================
    // Estado mutable (asignación de palabra)
    // ============================================

    /**
     * Palabra asignada a este elemento (null si no asignado)
     */
    private T palabraAsignada;

    /**
     * Indica si este elemento opcional fue rellenado por el generador (fallback)
     * en lugar de por criterio. Solo relevante para elementos opcionales.
     * Si es true, el frontend lo tratará como apoyo (sin botones SRS).
     */
    private boolean fueRellenadoPorGenerador;

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

    // ============================================
    // Consultas de tipo
    // ============================================

    /**
     * Indica si este elemento participa en la búsqueda por criterio (tiene criterioBusqueda).
     * Tanto slots puros como opcionales son slots.
     */
    public boolean esSlot() {
        return criterioBusqueda != null;
    }

    /**
     * Indica si este elemento es de apoyo puro (solo generador, sin criterio).
     */
    public boolean esApoyo() {
        return generadorObjeto != null && criterioBusqueda == null;
    }

    /**
     * Indica si este elemento es opcional (tiene criterio Y generador).
     */
    public boolean esOpcional() {
        return criterioBusqueda != null && generadorObjeto != null;
    }

    /**
     * Indica si este elemento es un slot obligatorio (solo criterio, sin generador).
     * La frase no se considera completa si un slot obligatorio no está asignado.
     */
    public boolean esSlotObligatorio() {
        return criterioBusqueda != null && generadorObjeto == null;
    }

    /**
     * Indica si este elemento fue rellenado por el generador (fallback).
     * Solo relevante para opcionales.
     */
    public boolean isFueRellenadoPorGenerador() {
        return fueRellenadoPorGenerador;
    }

    // ============================================
    // Lógica de asignación
    // ============================================

    /**
     * Verifica si una palabra cumple el criterio del slot.
     * Solo válido para elementos con criterio (slots y opcionales).
     *
     * @param palabra Palabra a verificar
     * @return true si el slot está vacío y la palabra cumple el criterio
     */
    public boolean coincide(PalabraFlexion<?> palabra) {
        if (!esSlot()) return false;
        return palabraAsignada == null && criterioBusqueda.cumple(palabra);
    }

    /**
     * Asigna una palabra al elemento (rellenado por criterio/SRS).
     *
     * @param palabra Palabra a asignar
     */
    @SuppressWarnings("unchecked")
    public void asignar(PalabraFlexion<?> palabra) {
        this.palabraAsignada = (T) palabra;
    }

    /**
     * Asigna una palabra al elemento marcándola como rellenada por generador.
     * Usado para opcionales que se rellenan por fallback.
     *
     * @param palabra Palabra a asignar
     */
    @SuppressWarnings("unchecked")
    public void asignarComoGenerado(PalabraFlexion<?> palabra) {
        this.palabraAsignada = (T) palabra;
        this.fueRellenadoPorGenerador = true;
    }

    /**
     * Verifica si el elemento tiene una palabra asignada.
     */
    public boolean estaAsignado() {
        return palabraAsignada != null;
    }

    /**
     * Genera el objeto usando el generador con el contexto de la frase.
     * Válido para elementos de apoyo y opcionales (que tienen generador).
     *
     * @param frase Estructura de frase con slots asignados
     * @return Objeto generado o null si no tiene generador o si el slot dependiente no está asignado
     */
    public T generarObjeto(EstructuraFrase frase) {
        if (generadorObjeto == null) return null;
        return generadorObjeto.apply(frase);
    }

    // ============================================
    // Extracción de texto para visualización
    // ============================================

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
    // Prioridad: extractor individual -> estrategia -> cadena vacía
    private String getDeEspanol(T p) {
        String resultado = extractorDeEspanol.apply(p);
        if (resultado == null && estrategiaExtraccion.deEspanol() != null) {
            resultado = estrategiaExtraccion.deEspanol().apply(p);
        }
        return resultado != null ? resultado : "";
    }

    private String getAEsloveno(T p) {
        String resultado = extractorAEsloveno.apply(p);
        if (resultado == null && estrategiaExtraccion.aEsloveno() != null) {
            resultado = estrategiaExtraccion.aEsloveno().apply(p);
        }
        return resultado != null ? resultado : "";
    }

    private String getDeEsloveno(T p) {
        String resultado = extractorDeEsloveno.apply(p);
        if (resultado == null && estrategiaExtraccion.deEsloveno() != null) {
            resultado = estrategiaExtraccion.deEsloveno().apply(p);
        }
        return resultado != null ? resultado : "";
    }

    private String getAEspanol(T p) {
        String resultado = extractorAEspanol.apply(p);
        if (resultado == null && estrategiaExtraccion.aEspanol() != null) {
            resultado = estrategiaExtraccion.aEspanol().apply(p);
        }
        return resultado != null ? resultado : "";
    }

    /**
     * Limpia el estado del elemento para reutilización.
     */
    public void limpiar() {
        this.palabraAsignada = null;
        this.fueRellenadoPorGenerador = false;
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
        private Function<T, String> extractorDeEspanol;
        private Function<T, String> extractorAEsloveno;
        private Function<T, String> extractorDeEsloveno;
        private Function<T, String> extractorAEspanol;

        public Builder<T> nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        /**
         * Configura criterio de búsqueda.
         * Si se combina con un generador, el elemento será OPCIONAL.
         * Si se usa solo, el elemento será un SLOT obligatorio.
         */
        public Builder<T> criterio(CriterioBusqueda<T> criterio) {
            this.criterioBusqueda = criterio;
            return this;
        }

        /**
         * Configura un generador que depende de otro elemento (slot dependiente).
         * La función recibe la palabra asignada al slot dependiente y genera el objeto.
         * Si el slot dependiente no está asignado, el generador devuelve null.
         * <p>
         * Si se combina con criterio, el elemento será OPCIONAL.
         * Si se usa solo, el elemento será un APOYO.
         *
         * @param slotDependiente Elemento del que depende este generador
         * @param generador Función que transforma la palabra del slot dependiente
         */
        public Builder<T> generador(ElementoFrase<?> slotDependiente,
                                    Function<PalabraFlexion<?>, T> generador) {
            this.slotDependiente = slotDependiente;
            this.generadorObjeto = frase -> {
                if (slotDependiente == null || !slotDependiente.estaAsignado()) return null;
                return generador.apply(slotDependiente.getPalabraAsignada());
            };
            return this;
        }

        /**
         * Configura un generador independiente (sin slot dependiente).
         * La función (Supplier) genera el objeto sin necesitar contexto.
         * <p>
         * Uso típico: obtener una palabra aleatoria de la BD como fallback para opcionales.
         * <p>
         * Si se combina con criterio, el elemento será OPCIONAL.
         * Si se usa solo, el elemento será un APOYO.
         *
         * @param generador Función sin argumentos que genera el objeto
         */
        public Builder<T> generador(Supplier<T> generador) {
            this.generadorObjeto = frase -> generador.get();
            return this;
        }

        /**
         * Estrategia de extracción singleton.
         */
        public Builder<T> extractor(EstrategiaExtraccion<T> estrategia) {
            this.estrategiaExtraccion = estrategia;
            return this;
        }

        // Extractores individuales (sobreescriben estrategia)
        public Builder<T> extractorDeEspanol(Function<T, String> extractor) {
            this.extractorDeEspanol = extractor;
            return this;
        }

        public Builder<T> extractorAEsloveno(Function<T, String> extractor) {
            this.extractorAEsloveno = extractor;
            return this;
        }

        public Builder<T> extractorDeEsloveno(Function<T, String> extractor) {
            this.extractorDeEsloveno = extractor;
            return this;
        }

        public Builder<T> extractorAEspanol(Function<T, String> extractor) {
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

            if (!tieneSlot && !tieneApoyo) {
                throw new IllegalStateException(
                        "ElementoFrase '" + nombre + "' debe tener criterio (slot), generador (apoyo), o ambos (opcional)");
            }

            // Verificar que tenga al menos una forma de extracción
            boolean sinExtractores = estrategiaExtraccion == null
                    && extractorDeEspanol == null
                    && extractorAEsloveno == null
                    && extractorDeEsloveno == null
                    && extractorAEspanol == null;
            if (sinExtractores) {
                throw new IllegalStateException(
                        "ElementoFrase '" + nombre + "' debe tener estrategia o extractores");
            }

            if (estrategiaExtraccion == null) estrategiaExtraccion = ExtraccionNull.get();
            if (extractorDeEspanol == null) extractorDeEspanol = p -> null;
            if (extractorAEsloveno == null) extractorAEsloveno = p -> null;
            if (extractorDeEsloveno == null) extractorDeEsloveno = p -> null;
            if (extractorAEspanol == null) extractorAEspanol = p -> null;

            return new ElementoFrase<>(this);
        }
    }
}

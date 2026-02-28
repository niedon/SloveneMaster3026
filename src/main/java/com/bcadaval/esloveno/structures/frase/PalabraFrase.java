package com.bcadaval.esloveno.structures.frase;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;
import com.bcadaval.esloveno.structures.extractores.EstrategiaExtraccion;
import com.bcadaval.esloveno.structures.extractores.ExtraccionNull;
import com.bcadaval.esloveno.structures.ModoVisualizacion;
import lombok.Getter;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Representa un hueco (slot) dentro de una {@link Frase}.
 * <p>
 * Cada {@code PalabraFrase} es una plantilla que:
 * <ol>
 *   <li>Determina si una {@link PalabraFlexion} disponible encaja en este hueco</li>
 *   <li>Expone sus criterios de elegibilidad para el sistema de consulta de palabras estudiables</li>
 * </ol>
 * <p>
 * <strong>Tipos funcionales según configuración:</strong>
 * <table>
 *   <tr><th>criterio</th><th>generador</th><th>Tipo</th><th>Descripción</th></tr>
 *   <tr><td>✅</td><td>❌</td><td>Obligatoria</td><td>Se rellena desde SRS, bloquea la frase si no se rellena</td></tr>
 *   <tr><td>❌</td><td>✅</td><td>Apoyo</td><td>Se genera tras rellenar obligatorias, nunca participa en SRS</td></tr>
 *   <tr><td>✅</td><td>✅</td><td>Opcional</td><td>Intenta SRS; si falla, usa generador como fallback (no SRS)</td></tr>
 *   <tr><td>❌</td><td>❌</td><td>Inválida</td><td>Error: frase marcada como inválida</td></tr>
 * </table>
 * <p>
 * <strong>Ejemplo de construcción:</strong>
 * <pre>
 * PalabraFrase&lt;SustantivoFlexion&gt; sustantivo = PalabraFrase.&lt;SustantivoFlexion&gt;builder()
 *     .nombre("SUSTANTIVO")
 *     .criterio(CriterioBusquedaNuevo.de(SustantivoFlexion.class)
 *         .conCaso(Caso.NOMINATIVO)
 *         .build())
 *     .extractor(ExtractorSustantivo.get())
 *     .build();
 * </pre>
 *
 * @param <T> Tipo concreto de {@link PalabraFlexion} que maneja este hueco
 */
public class PalabraFrase<T extends PalabraFlexion<?>> {

    // ============================================
    // Campos inmutables (configuración)
    // ============================================

    @Getter
    private final String nombre;
    @Getter
    private final CriterioBusquedaNuevo<T> criterioBusqueda;
    private final Function<Frase, T> generadorObjeto;
    private final EstrategiaExtraccion<T> estrategiaExtraccion;
    private final Function<T, String> extractorDeEspanol;
    private final Function<T, String> extractorAEsloveno;
    private final Function<T, String> extractorDeEsloveno;
    private final Function<T, String> extractorAEspanol;

    // ============================================
    // Estado mutable (asignación de palabra)
    // ============================================

    /**
     * Palabra asignada a este hueco (null si vacío).
     */
    @Getter
    private T palabraAsignada;

    /**
     * Indica si este hueco fue rellenado por el generador (fallback)
     * en lugar de por criterio. Solo relevante para huecos opcionales.
     * Si es true, el frontend lo tratará como apoyo (sin botones SRS).
     */
    @Getter
    private boolean fueRellenadoPorGenerador;

    private PalabraFrase(Builder<T> builder) {
        this.nombre = builder.nombre;
        this.criterioBusqueda = builder.criterioBusqueda;
        this.generadorObjeto = builder.generadorObjeto;
        this.estrategiaExtraccion = builder.estrategiaExtraccion;
        this.extractorDeEspanol = builder.extractorDeEspanol;
        this.extractorAEsloveno = builder.extractorAEsloveno;
        this.extractorDeEsloveno = builder.extractorDeEsloveno;
        this.extractorAEspanol = builder.extractorAEspanol;
    }

    // ============================================
    // Consultas de tipo funcional
    // ============================================

    /**
     * Indica si este hueco participa en la búsqueda por criterio (tiene {@code criterioBusqueda}).
     */
    public boolean tieneCriterio() {
        return criterioBusqueda != null;
    }

    /**
     * Indica si este hueco tiene generador.
     */
    public boolean tieneGenerador() {
        return generadorObjeto != null;
    }

    /**
     * Indica si es un apoyo puro (solo generador, sin criterio).
     */
    public boolean esApoyo() {
        return generadorObjeto != null && criterioBusqueda == null;
    }

    /**
     * Indica si es opcional (tiene criterio Y generador).
     */
    public boolean esOpcional() {
        return criterioBusqueda != null && generadorObjeto != null;
    }

    /**
     * Indica si es un slot obligatorio (solo criterio, sin generador).
     */
    public boolean esObligatorio() {
        return criterioBusqueda != null && generadorObjeto == null;
    }

    /**
     * Indica si este hueco tiene una palabra asignada.
     */
    public boolean estaAsignado() {
        return palabraAsignada != null;
    }

    /**
     * Indica si este hueco participa en SRS.
     * Un hueco participa en SRS si tiene criterio, está asignado y NO fue rellenado por generador.
     *
     * @return {@code true} si participa en SRS
     */
    public boolean participaEnSRS() {
        return tieneCriterio() && estaAsignado() && !fueRellenadoPorGenerador;
    }

    // ============================================
    // Lógica de asignación
    // ============================================

    /**
     * Verifica si una {@link PalabraFlexion} cumple los criterios de este hueco
     * y puede ser asignada.
     * <p>
     * Comprueba:
     * <ol>
     *   <li>Que el hueco tenga criterio y esté vacío</li>
     *   <li>Que la palabra cumpla los criterios fijos</li>
     *   <li>Que las dependencias estén resueltas y la palabra cumpla los criterios dinámicos</li>
     * </ol>
     *
     * @param palabra palabra candidata
     * @return {@code true} si la palabra puede ser asignada a este hueco
     */
    public boolean coincide(PalabraFlexion<?> palabra) {
        if (!tieneCriterio()) return false;
        if (estaAsignado()) return false;
        return criterioBusqueda.cumpleConDependencias(palabra);
    }

    /**
     * Asigna una palabra al hueco (rellenado por criterio SRS).
     *
     * @param palabra palabra a asignar
     */
    @SuppressWarnings("unchecked")
    public void asignar(PalabraFlexion<?> palabra) {
        this.palabraAsignada = (T) palabra;
        this.fueRellenadoPorGenerador = false;
    }

    /**
     * Asigna una palabra al hueco marcándola como rellenada por generador.
     * Usado para opcionales que se rellenan por fallback y para apoyos.
     *
     * @param palabra palabra generada
     */
    public void asignarComoGenerado(T palabra) {
        this.palabraAsignada = palabra;
        this.fueRellenadoPorGenerador = true;
    }

    /**
     * Genera el objeto usando el generador con el contexto de la frase.
     *
     * @param frase la frase contenedora con sus huecos asignados
     * @return objeto generado, o {@code null} si no tiene generador
     */
    public T generarObjeto(Frase frase) {
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
     * Limpia el estado mutable para reutilización (patrón singleton).
     */
    public void limpiar() {
        this.palabraAsignada = null;
        this.fueRellenadoPorGenerador = false;
    }

    // ============================================
    // Builder
    // ============================================

    /**
     * Crea un nuevo builder para {@code PalabraFrase}.
     *
     * @param <T> tipo de flexión
     * @return builder nuevo
     */
    public static <T extends PalabraFlexion<?>> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Builder para construir instancias de {@link PalabraFrase}.
     * <p>
     * Requiere al menos un nombre y una forma de extracción (extractor o estrategia).
     * Debe tener criterio, generador o ambos (nunca ninguno).
     *
     * @param <T> tipo de flexión
     */
    public static class Builder<T extends PalabraFlexion<?>> {
        private String nombre;
        private CriterioBusquedaNuevo<T> criterioBusqueda;
        private Function<Frase, T> generadorObjeto;
        private EstrategiaExtraccion<T> estrategiaExtraccion;
        private Function<T, String> extractorDeEspanol;
        private Function<T, String> extractorAEsloveno;
        private Function<T, String> extractorDeEsloveno;
        private Function<T, String> extractorAEspanol;

        /**
         * Nombre identificador del hueco (ej: "VERBO", "CD", "PRONOMBRE").
         */
        public Builder<T> nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        /**
         * Criterio de búsqueda para slots y opcionales.
         */
        public Builder<T> criterio(CriterioBusquedaNuevo<T> criterio) {
            this.criterioBusqueda = criterio;
            return this;
        }

        /**
         * Generador dependiente de otro hueco.
         * La función recibe la palabra asignada al hueco dependiente y genera el objeto.
         *
         * @param huecoFuente hueco del que depende este generador
         * @param generador   función que transforma la palabra del hueco fuente
         * @param <S>         tipo de flexión del hueco fuente
         */
        public <S extends PalabraFlexion<?>> Builder<T> generador(
                PalabraFrase<S> huecoFuente,
                Function<S, T> generador) {
            this.generadorObjeto = frase -> {
                if (!huecoFuente.estaAsignado()) return null;
                return generador.apply(huecoFuente.getPalabraAsignada());
            };
            return this;
        }

        /**
         * Generador independiente (sin dependencia de otro hueco).
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

        /**
         * Construye la {@link PalabraFrase}.
         *
         * @return instancia inmutable (configuración) con estado mutable (asignación)
         * @throws IllegalStateException si falta nombre, extractor, o no tiene ni criterio ni generador
         */
        public PalabraFrase<T> build() {
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalStateException("El nombre del hueco es obligatorio");
            }

            boolean tieneCriterio = criterioBusqueda != null;
            boolean tieneGenerador = generadorObjeto != null;

            if (!tieneCriterio && !tieneGenerador) {
                throw new IllegalStateException(
                        "PalabraFrase '" + nombre + "' debe tener criterio, generador o ambos");
            }

            boolean sinExtractores = estrategiaExtraccion == null
                    && extractorDeEspanol == null
                    && extractorAEsloveno == null
                    && extractorDeEsloveno == null
                    && extractorAEspanol == null;
            if (sinExtractores) {
                throw new IllegalStateException(
                        "PalabraFrase '" + nombre + "' debe tener estrategia o extractores");
            }

            if (estrategiaExtraccion == null) estrategiaExtraccion = ExtraccionNull.get();
            if (extractorDeEspanol == null) extractorDeEspanol = p -> null;
            if (extractorAEsloveno == null) extractorAEsloveno = p -> null;
            if (extractorDeEsloveno == null) extractorDeEsloveno = p -> null;
            if (extractorAEspanol == null) extractorAEspanol = p -> null;

            return new PalabraFrase<>(this);
        }
    }
}


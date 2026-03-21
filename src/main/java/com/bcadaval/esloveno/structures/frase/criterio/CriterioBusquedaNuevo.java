package com.bcadaval.esloveno.structures.frase.criterio;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.*;
import com.bcadaval.esloveno.structures.frase.dependencia.Dependencia;
import lombok.Getter;

import java.util.*;

/**
 * Criterio de búsqueda tipado para un hueco ({@link com.bcadaval.esloveno.structures.frase.PalabraFrase}).
 * <p>
 * Encapsula las restricciones gramaticales que una {@link PalabraFlexion} debe cumplir
 * para ser asignada a un hueco. Cada restricción se almacena como un conjunto de valores
 * posibles (semántica OR intra-característica) y se combinan entre sí con AND.
 * <p>
 * Adicionalmente puede contener {@link Dependencia dependencias} de otros huecos
 * que aportan criterios adicionales en tiempo de asignación.
 * <p>
 * <strong>Punto de entrada:</strong>
 * <pre>
 * CriterioBusquedaNuevo.de(SustantivoFlexion.class)  → SustantivoCriterioBuilder
 * CriterioBusquedaNuevo.de(VerboFlexion.class)        → VerboCriterioBuilder
 * CriterioBusquedaNuevo.de(AdjetivoFlexion.class)     → AdjetivoCriterioBuilder
 * CriterioBusquedaNuevo.de(NumeralFlexion.class)      → NumeralCriterioBuilder
 * CriterioBusquedaNuevo.de(PronombreFlexion.class)    → PronombreCriterioBuilder
 * </pre>
 * <p>
 * <strong>Ejemplo de uso:</strong>
 * <pre>
 * CriterioBusquedaNuevo&lt;SustantivoFlexion&gt; criterio =
 *     CriterioBusquedaNuevo.de(SustantivoFlexion.class)
 *         .conCaso(Caso.NOMINATIVO)
 *         .conGenero(Genero.FEMENINO, Genero.NEUTRO)
 *         .build();
 * </pre>
 *
 * @param <T> Tipo concreto de {@link PalabraFlexion} al que se dirige este criterio
 */
@Getter
public class CriterioBusquedaNuevo<T extends PalabraFlexion<?>> {

    /**
     * Clase del tipo de flexión que busca este criterio.
     * -- GETTER --
     *  Obtiene la clase del tipo de flexión objetivo.
     */
    private final Class<T> tipoFlexion;

    /**
     * Mapa de restricciones fijas: nombre de campo → conjunto de valores válidos (OR entre ellos).
     * Varias entradas en el mapa se combinan con AND.
     * -- GETTER --
     *  Obtiene las restricciones fijas de este criterio.
     *  Cada entrada mapea un nombre de campo a los valores aceptados (OR).
     */
    private final Map<String, Set<Object>> restricciones;

    /**
     * Lista de dependencias condicionales de otros huecos.
     * Cada dependencia aporta criterios adicionales evaluados en tiempo de asignación.
     * -- GETTER --
     *  Obtiene las dependencias condicionales de este criterio.
     */
    private final List<Dependencia<?>> dependencias;

    /**
     * Restricciones numéricas (rangos, mayor/menor que, valores discretos).
     * Se evalúan por separado de las restricciones de igualdad.
     * -- GETTER --
     *  Obtiene las restricciones numéricas de este criterio.
     */
    private final List<RestriccionNumerica> restriccionesNumericas;

    CriterioBusquedaNuevo(Class<T> tipoFlexion,
                          Map<String, Set<Object>> restricciones,
                          List<Dependencia<?>> dependencias,
                          List<RestriccionNumerica> restriccionesNumericas) {
        this.tipoFlexion = tipoFlexion;
        this.restricciones = Collections.unmodifiableMap(restricciones);
        this.dependencias = Collections.unmodifiableList(dependencias);
        this.restriccionesNumericas = Collections.unmodifiableList(restriccionesNumericas);
    }

    /**
     * Verifica si una {@link PalabraFlexion} cumple los criterios fijos de este criterio.
     * <strong>No evalúa dependencias</strong>; para evaluación completa usar
     * {@link #cumpleConDependencias(PalabraFlexion)}.
     *
     * @param palabra la palabra a verificar
     * @return {@code true} si la palabra es del tipo correcto y cumple todas las restricciones fijas
     */
    public boolean cumpleCriteriosFijos(PalabraFlexion<?> palabra) {
        if (palabra == null) return false;
        if (!tipoFlexion.isInstance(palabra)) return false;

        T palabraTipada = tipoFlexion.cast(palabra);

        // Evaluar restricciones de igualdad (AND entre campos, OR entre valores de cada campo)
        boolean cumpleIgualdad = restricciones.entrySet().stream()
                .allMatch(entry -> {
                    String campo = entry.getKey();
                    // Restricción de exclusión: !campo significa "NO debe estar en estos valores"
                    if (campo.startsWith("!")) {
                        String campoReal = campo.substring(1);
                        Object valorReal = extraerValor(palabraTipada, campoReal);
                        return !entry.getValue().contains(valorReal);
                    }
                    Object valorReal = extraerValor(palabraTipada, campo);
                    return entry.getValue().contains(valorReal);
                });

        if (!cumpleIgualdad) return false;

        // Evaluar restricciones numéricas (AND entre ellas)
        for (RestriccionNumerica restriccion : restriccionesNumericas) {
            Object valorReal = extraerValor(palabraTipada, restriccion.campo());
            Integer valorEntero = (valorReal instanceof Integer i) ? i : null;
            if (!restriccion.cumple(valorEntero)) return false;
        }

        return true;
    }

    /**
     * Verifica si una {@link PalabraFlexion} cumple tanto los criterios fijos
     * como todos los criterios aportados por dependencias ya resueltas.
     * <p>
     * Una dependencia no resuelta (cuyo hueco padre no tiene palabra asignada)
     * provoca que esta función devuelva {@code false}, ya que no se puede evaluar.
     *
     * @param palabra la palabra candidata
     * @return {@code true} si cumple criterios fijos y todos los criterios dinámicos
     */
    public boolean cumpleConDependencias(PalabraFlexion<?> palabra) {
        if (!cumpleCriteriosFijos(palabra)) return false;

        for (Dependencia<?> dependencia : dependencias) {
            if (!dependencia.estaResuelta()) return false;
            CriterioBusquedaNuevo<?> criterioAdicional = dependencia.resolver();
            if (criterioAdicional == null) return false;
            if (!criterioAdicional.cumpleCriteriosFijos(palabra)) return false;
        }

        return true;
    }

    /**
     * Indica si este criterio tiene dependencias de otros huecos.
     *
     * @return {@code true} si hay al menos una dependencia
     */
    public boolean tieneDependencias() {
        return !dependencias.isEmpty();
    }

    /**
     * Indica si todas las dependencias de este criterio están resueltas
     * (sus huecos padre tienen una palabra asignada).
     *
     * @return {@code true} si no hay dependencias o todas están resueltas
     */
    public boolean dependenciasResueltas() {
        return dependencias.stream().allMatch(Dependencia::estaResuelta);
    }

    /**
     * Obtiene todos los criterios posibles que podrían resultar de las dependencias.
     * Se usa para calcular las palabras estudiables: se expande el producto cartesiano
     * de todas las ramas posibles de cada dependencia y se combina con los criterios fijos.
     * <p>
     * Si no hay dependencias, devuelve una lista con un único elemento: este mismo criterio.
     * Si hay N dependencias con M1, M2, ... Mn ramas cada una, devuelve M1×M2×...×Mn criterios.
     *
     * @return lista de todos los criterios expandidos posibles
     */
    public List<CriterioBusquedaNuevo<T>> expandirDependencias() {
        if (dependencias.isEmpty()) {
            return List.of(this);
        }

        // Recoger todas las posibles ramas de cada dependencia
        List<List<CriterioBusquedaNuevo<?>>> ramasPorDependencia = new ArrayList<>();
        for (Dependencia<?> dependencia : dependencias) {
            ramasPorDependencia.add(dependencia.obtenerTodasLasRamas());
        }

        // Producto cartesiano de todas las combinaciones
        List<List<CriterioBusquedaNuevo<?>>> combinaciones = productoCartesiano(ramasPorDependencia);

        // Para cada combinación, fusionar criterios fijos + criterios de cada rama
        List<CriterioBusquedaNuevo<T>> resultado = new ArrayList<>();
        for (List<CriterioBusquedaNuevo<?>> combinacion : combinaciones) {
            Map<String, Set<Object>> restriccionesFusionadas = new LinkedHashMap<>(this.restricciones);
            List<RestriccionNumerica> numericasFusionadas = new ArrayList<>(this.restriccionesNumericas);

            for (CriterioBusquedaNuevo<?> rama : combinacion) {
                for (Map.Entry<String, Set<Object>> entry : rama.getRestricciones().entrySet()) {
                    restriccionesFusionadas.merge(entry.getKey(), new LinkedHashSet<>(entry.getValue()),
                            (existente, nueva) -> {
                                Set<Object> merged = new LinkedHashSet<>(existente);
                                merged.addAll(nueva);
                                return merged;
                            });
                }
                numericasFusionadas.addAll(rama.getRestriccionesNumericas());
            }
            resultado.add(new CriterioBusquedaNuevo<>(tipoFlexion, restriccionesFusionadas,
                    List.of(), numericasFusionadas));
        }

        return resultado;
    }

    /**
     * Punto de entrada tipado para crear un builder de criterio.
     * Devuelve el builder específico según el tipo de flexión.
     * <p>
     * El cast es seguro porque cada rama crea un builder cuyo tipo genérico coincide
     * con la clase proporcionada. El compilador infiere el tipo en el sitio de llamada:
     * <pre>
     * // El tipo se infiere correctamente:
     * SustantivoCriterioBuilder b = CriterioBusquedaNuevo.de(SustantivoFlexion.class);
     * </pre>
     *
     * @param tipoFlexion clase de la flexión objetivo
     * @param <T>         tipo de la flexión
     * @param <B>         tipo del builder concreto
     * @return builder específico para ese tipo
     * @throws IllegalArgumentException si el tipo no tiene builder específico
     */
    @SuppressWarnings("unchecked")
    public static <T extends PalabraFlexion<?>, B extends CriterioBuilderBase<T, ?>> B de(Class<T> tipoFlexion) {
        if (tipoFlexion == SustantivoFlexion.class) {
            return (B) new SustantivoCriterioBuilder();
        } else if (tipoFlexion == VerboFlexion.class) {
            return (B) new VerboCriterioBuilder();
        } else if (tipoFlexion == AdjetivoFlexion.class) {
            return (B) new AdjetivoCriterioBuilder();
        } else if (tipoFlexion == NumeralFlexion.class) {
            return (B) new NumeralCriterioBuilder();
        } else if (tipoFlexion == PronombreFlexion.class) {
            return (B) new PronombreCriterioBuilder();
        } else if (tipoFlexion == ParticulaFlexion.class) {
            return (B) new ParticulaCriterioBuilder();
        }
        throw new IllegalArgumentException("Tipo de flexión no soportado: " + tipoFlexion.getSimpleName());
    }

    // ========================================================================
    // Métodos internos
    // ========================================================================

    /**
     * Extrae el valor de un campo de una PalabraFlexion usando reflexión controlada.
     * Los nombres de campo corresponden a los campos de las entidades JPA
     * y de sus palabras base (para características heredadas como género de sustantivo).
     */
    static Object extraerValor(PalabraFlexion<?> palabra, String nombreCampo) {
        try {
            // Primero buscar en la flexión directamente
            var campo = buscarCampo(palabra.getClass(), nombreCampo);
            if (campo != null) {
                campo.setAccessible(true);
                return campo.get(palabra);
            }

            // Si no se encuentra, buscar en la palabra base
            // Los campos con prefijo "base." son de la palabra base
            if (nombreCampo.startsWith("base.")) {
                String campoBase = nombreCampo.substring(5);
                Object palabraBase = obtenerPalabraBase(palabra);
                if (palabraBase != null) {
                    var campoBaseField = buscarCampo(palabraBase.getClass(), campoBase);
                    if (campoBaseField != null) {
                        campoBaseField.setAccessible(true);
                        return campoBaseField.get(palabraBase);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error accediendo al campo '" + nombreCampo + "' en " + palabra.getClass().getSimpleName(), e);
        }
        return null;
    }

    private static java.lang.reflect.Field buscarCampo(Class<?> clazz, String nombre) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(nombre);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static Object obtenerPalabraBase(PalabraFlexion<?> palabra) {
        if (palabra instanceof SustantivoFlexion sf) return sf.getSustantivoBase();
        if (palabra instanceof VerboFlexion vf) return vf.getVerboBase();
        if (palabra instanceof AdjetivoFlexion af) return af.getAdjetivoBase();
        if (palabra instanceof NumeralFlexion nf) return nf.getNumeralBase();
        if (palabra instanceof PronombreFlexion pf) return pf.getPronombreBase();
        if (palabra instanceof ParticulaFlexion paf) return paf.getParticulaBase();
        return null;
    }

    /**
     * Calcula el producto cartesiano de una lista de listas.
     */
    private static <E> List<List<E>> productoCartesiano(List<List<E>> listas) {
        List<List<E>> resultado = new ArrayList<>();
        if (listas.isEmpty()) {
            resultado.add(new ArrayList<>());
            return resultado;
        }

        List<E> primeraLista = listas.getFirst();
        List<List<E>> resto = productoCartesiano(listas.subList(1, listas.size()));

        for (E elemento : primeraLista) {
            for (List<E> combinacionResto : resto) {
                List<E> combinacion = new ArrayList<>();
                combinacion.add(elemento);
                combinacion.addAll(combinacionResto);
                resultado.add(combinacion);
            }
        }

        return resultado;
    }
}




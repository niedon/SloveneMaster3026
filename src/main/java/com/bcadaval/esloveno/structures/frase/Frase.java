package com.bcadaval.esloveno.structures.frase;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.services.RepeticionEspaciadaService;
import com.bcadaval.esloveno.structures.DatoVisualizacion;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.FraseTipoPalabra;
import com.bcadaval.esloveno.structures.ModoVisualizacion;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;
import com.bcadaval.esloveno.structures.frase.dependencia.Dependencia;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.*;

/**
 * Clase base abstracta del sistema de frases.
 * <p>
 * Contiene una lista ordenada de {@link PalabraFrase} (huecos) que se rellenan con
 * {@link PalabraFlexion}s según criterios gramaticales. Los huecos pueden tener
 * dependencias condicionales entre sí formando un DAG.
 * <p>
 * <strong>Validación al arranque:</strong>
 * <ul>
 *   <li>Se detectan ciclos en el grafo de dependencias (algoritmo de Kahn)</li>
 *   <li>Se verifica que ningún hueco carezca de criterio y generador</li>
 *   <li>Las frases inválidas se marcan con {@link #invalida} = {@code true}
 *       y un {@link #motivoInvalidez} descriptivo</li>
 * </ul>
 * <p>
 * <strong>Asignación ({@code /getWords}):</strong>
 * <ol>
 *   <li>Cada palabra candidata pasa por cada hueco con criterio</li>
 *   <li>Los huecos con dependencias no resueltas rechazan candidatas automáticamente</li>
 *   <li>Múltiples pasadas hasta que una pasada no produzca asignaciones</li>
 *   <li>Tras asignar, los huecos opcionales sin asignar se rellenan por generador</li>
 * </ol>
 * <p>
 * Las implementaciones deben ser beans de Spring ({@code @Component}) y estar anotadas
 * con {@link DificultadFrase} para indicar su nivel de dificultad.
 * <p>
 * <strong>ADVERTENCIA DE CONCURRENCIA:</strong> Esta clase es un singleton de Spring
 * con estado mutable ({@code palabraAsignada} en cada {@link PalabraFrase}).
 * {@link #limpiar()} debe llamarse antes de cada reutilización. Seguro para uso monousuario.
 */
@Log4j2
public abstract class Frase {

    /**
     * Lista ordenada de todos los huecos (determina el orden de visualización).
     */
    protected final List<PalabraFrase<? extends PalabraFlexion<?>>> elementos = new ArrayList<>();

    /**
     * Set de identificadores de PalabraFlexion ya asignadas a esta frase en la sesión actual.
     * Clave: "sloleksId:id" para evitar duplicados.
     */
    private final Set<String> palabrasAsignadas = new HashSet<>();

    /**
     * Indica si la frase tiene un error estructural (ej: dependencia circular).
     */
    @Getter
    private boolean invalida = false;

    /**
     * Descripción del error si la frase es inválida.
     */
    @Getter
    private String motivoInvalidez;

    // ============================================
    // Configuración de estructura
    // ============================================

    /**
     * Añade un hueco a la estructura. El orden de adición determina el orden de visualización.
     *
     * @param elemento hueco a añadir
     */
    protected void agregarElemento(PalabraFrase<? extends PalabraFlexion<?>> elemento) {
        elementos.add(elemento);
    }

    // ============================================
    // Validación DAG (algoritmo de Kahn)
    // ============================================

    /**
     * Valida la estructura de la frase:
     * <ul>
     *   <li>Detecta ciclos en el grafo de dependencias entre huecos</li>
     *   <li>Verifica que ningún hueco carezca de criterio y generador</li>
     * </ul>
     * <p>
     * Marca la frase como inválida si encuentra errores.
     * Este método debe llamarse al arrancar la aplicación.
     */
    public void validar() {
        // Validar que ningún hueco carece de criterio y generador
        for (PalabraFrase<?> elemento : elementos) {
            if (!elemento.tieneCriterio() && !elemento.tieneGenerador()) {
                marcarInvalida("El hueco '" + elemento.getNombre() + "' no tiene ni criterio ni generador");
                return;
            }
        }

        // Validar DAG con algoritmo de Kahn
        if (!validarAusenciaDeCiclos()) {
            marcarInvalida("Dependencia circular detectada entre huecos");
            return;
        }

        // Si pasó todas las validaciones, es válida
        this.invalida = false;
        this.motivoInvalidez = null;
    }

    /**
     * Detecta ciclos en el grafo de dependencias usando el algoritmo de Kahn (topological sort).
     * <p>
     * Construye un grafo dirigido donde una arista A→B significa "A depende de B".
     * Si el sort topológico no puede procesar todos los nodos, hay un ciclo.
     *
     * @return {@code true} si no hay ciclos, {@code false} si hay ciclo
     */
    private boolean validarAusenciaDeCiclos() {
        // Mapear cada PalabraFrase a un índice
        Map<PalabraFrase<?>, Integer> indice = new IdentityHashMap<>();
        for (int i = 0; i < elementos.size(); i++) {
            indice.put(elementos.get(i), i);
        }

        int n = elementos.size();
        int[] gradoEntrada = new int[n];
        List<List<Integer>> adyacencia = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adyacencia.add(new ArrayList<>());
        }

        // Construir grafo: si el elemento i depende del elemento j, arista j→i
        for (int i = 0; i < n; i++) {
            PalabraFrase<?> elem = elementos.get(i);
            if (elem.getCriterioBusqueda() != null) {
                for (Dependencia<?> dep : elem.getCriterioBusqueda().getDependencias()) {
                    PalabraFrase<?> padre = dep.getHuecoReferenciado();
                    Integer j = indice.get(padre);
                    if (j != null) {
                        adyacencia.get(j).add(i);
                        gradoEntrada[i]++;
                    }
                }
            }
        }

        // Kahn: BFS desde nodos con grado de entrada 0
        Queue<Integer> cola = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (gradoEntrada[i] == 0) {
                cola.add(i);
            }
        }

        int procesados = 0;
        while (!cola.isEmpty()) {
            int nodo = cola.poll();
            procesados++;
            for (int vecino : adyacencia.get(nodo)) {
                gradoEntrada[vecino]--;
                if (gradoEntrada[vecino] == 0) {
                    cola.add(vecino);
                }
            }
        }

        return procesados == n;
    }

    private void marcarInvalida(String motivo) {
        this.invalida = true;
        this.motivoInvalidez = motivo;
        log.warn("Frase '{}' marcada como inválida: {}", getNombreMostrar(), motivo);
    }

    // ============================================
    // Asignación de palabras
    // ============================================

    /**
     * Intenta asignar una palabra a algún hueco vacío con criterio de esta frase.
     * <p>
     * Primero verifica que la palabra no esté ya asignada a esta frase
     * (misma combinación sloleksId + id). Luego itera por los huecos con criterio
     * y asigna la primera coincidencia.
     *
     * @param palabra palabra candidata
     * @return {@code true} si se asignó a algún hueco
     */
    public boolean intentarAsignar(PalabraFlexion<?> palabra) {
        String clave = generarClave(palabra);
        if (palabrasAsignadas.contains(clave)) {
            return false;
        }

        for (PalabraFrase<?> elem : elementos) {
            if (elem.coincide(palabra)) {
                elem.asignar(palabra);
                palabrasAsignadas.add(clave);
                log.debug("Asignado {} a hueco '{}' en frase '{}'",
                        palabra.getClass().getSimpleName(), elem.getNombre(), getNombreMostrar());
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si todos los huecos obligatorios tienen una palabra asignada.
     * Los huecos opcionales (con criterio + generador) NO bloquean la completitud.
     * Los huecos de apoyo tampoco bloquean (se generan después).
     */
    public boolean estaCompleta() {
        return elementos.stream()
                .filter(PalabraFrase::esObligatorio)
                .allMatch(PalabraFrase::estaAsignado);
    }

    /**
     * Calcula la media de {@code proximaRevision} de los huecos que participan en SRS.
     * <p>
     * Solo se consideran huecos rellenados por criterio (no por generador).
     * Las palabras nuevas (sin {@code proximaRevision}) se mapean a {@link Instant#now()}.
     *
     * @return el {@link Instant} medio, o {@code Instant.MIN} si no hay huecos SRS
     */
    public Instant calcularMediaInstant() {
        return Instant.ofEpochMilli((long) elementos.stream()
                .filter(PalabraFrase::participaEnSRS)
                .map(PalabraFrase::getPalabraAsignada)
                .filter(Objects::nonNull)
                .map(p -> p.getProximaRevision() != null ? p.getProximaRevision() : Instant.now())
                .mapToLong(Instant::toEpochMilli)
                .average()
                .orElse((double) Instant.now().toEpochMilli()));
    }

    // ============================================
    // Construcción de datos de visualización
    // ============================================

    /**
     * Construye la lista de datos de visualización para el JSP.
     * <p>
     * <strong>Orden:</strong>
     * <ol>
     *   <li>Opcionales no asignados → se rellenan con generador (fallback)</li>
     *   <li>Apoyos → se generan (pueden depender de opcionales ya rellenados)</li>
     * </ol>
     * Si cualquier generador devuelve null, la frase se descarta (devuelve null).
     *
     * @param repeticionEspaciadaService servicio SRS para calcular intervalos
     * @return lista de datos de visualización, o null si algún generador falló
     */
    public List<DatoVisualizacion> construirDatosVisualizacion(RepeticionEspaciadaService repeticionEspaciadaService) {
        ModoVisualizacion modo = ModoVisualizacion.aleatorio();
        log.debug("Construyendo datos con modo: {}", modo);

        // 1. Rellenar opcionales no asignados con generador (fallback)
        for (var elem : elementos) {
            if (elem.esOpcional() && !elem.estaAsignado()) {
                if (!generarYAsignar(elem)) {
                    log.error("FRASE DESCARTADA '{}': el opcional '{}' no pudo ser generado",
                            getNombreMostrar(), elem.getNombre());
                    return null;
                }
            }
        }

        // 2. Generar apoyos
        for (var elem : elementos) {
            if (elem.esApoyo() && !elem.estaAsignado()) {
                if (!generarYAsignar(elem)) {
                    log.error("FRASE DESCARTADA '{}': el apoyo '{}' no pudo ser generado",
                            getNombreMostrar(), elem.getNombre());
                    return null;
                }
            }
        }

        // 3. Construir datos en orden
        return elementos.stream()
                .map(el -> construirDato(el, modo, repeticionEspaciadaService))
                .filter(Objects::nonNull)
                .toList();
    }

    private <T extends PalabraFlexion<?>> boolean generarYAsignar(PalabraFrase<T> elem) {
        T generado = elem.generarObjeto(this);
        if (generado == null) return false;
        elem.asignarComoGenerado(generado);
        log.debug("Hueco '{}' rellenado por generador", elem.getNombre());
        return true;
    }

    private DatoVisualizacion construirDato(PalabraFrase<?> elem, ModoVisualizacion modo,
                                            RepeticionEspaciadaService srsService) {
        if (!elem.estaAsignado()) return null;

        PalabraFlexion<?> palabra = elem.getPalabraAsignada();
        boolean participaEnSRS = elem.participaEnSRS();

        String intervaloArriba = null;
        String intervaloAbajo = null;

        if (participaEnSRS && srsService != null) {
            try {
                long segundosArriba = srsService.calcularProximoIntervalo(palabra, true);
                long segundosAbajo = srsService.calcularProximoIntervalo(palabra, false);
                intervaloArriba = DatoVisualizacion.formatearIntervalo(segundosArriba);
                intervaloAbajo = DatoVisualizacion.formatearIntervalo(segundosAbajo);
            } catch (Exception e) {
                log.warn("Error calculando intervalos para {}: {}", palabra.getFlexion(), e.getMessage());
            }
        }

        return DatoVisualizacion.builder()
                .textoFila1(elem.getTextoFila1(modo))
                .textoFila2(elem.getTextoFila2(modo))
                .id(participaEnSRS ? palabra.getId() : null)
                .tipo(participaEnSRS ? FraseTipoPalabra.fromObject(palabra) : null)
                .intervaloArriba(intervaloArriba)
                .intervaloAbajo(intervaloAbajo)
                .build();
    }

    // ============================================
    // Criterios de búsqueda
    // ============================================

    /**
     * Obtiene los criterios de búsqueda de todos los huecos con criterio.
     *
     * @return lista de criterios
     */
    public List<CriterioBusquedaNuevo<?>> getCriteriosBusqueda() {
        List<CriterioBusquedaNuevo<?>> criterios = new ArrayList<>();
        for (PalabraFrase<?> elem : elementos) {
            if (elem.tieneCriterio()) {
                criterios.add(elem.getCriterioBusqueda());
            }
        }
        return criterios;
    }

    /**
     * Obtiene los casos gramaticales usados por esta frase,
     * extraídos de las restricciones de los criterios.
     */
    public Set<Caso> getCasosUsados() {
        Set<Caso> casos = new HashSet<>();
        for (PalabraFrase<?> elem : elementos) {
            if (elem.getCriterioBusqueda() == null) continue;
            Set<Object> valoresCaso = elem.getCriterioBusqueda().getRestricciones().get("caso");
            if (valoresCaso != null) {
                valoresCaso.stream()
                        .filter(Caso.class::isInstance)
                        .map(Caso.class::cast)
                        .forEach(casos::add);
            }
        }
        return casos;
    }

    /**
     * Obtiene las formas verbales usadas por esta frase,
     * extraídas de las restricciones de los criterios.
     */
    public Set<FormaVerbal> getFormasVerbalesUsadas() {
        Set<FormaVerbal> formas = new HashSet<>();
        for (PalabraFrase<?> elem : elementos) {
            if (elem.getCriterioBusqueda() == null) continue;
            Set<Object> valoresForma = elem.getCriterioBusqueda().getRestricciones().get("formaVerbal");
            if (valoresForma != null) {
                valoresForma.stream()
                        .filter(FormaVerbal.class::isInstance)
                        .map(FormaVerbal.class::cast)
                        .forEach(formas::add);
            }
        }
        return formas;
    }

    // ============================================
    // Ciclo de vida
    // ============================================

    /**
     * Limpia todos los huecos y el set de palabras asignadas para reutilización.
     */
    public void limpiar() {
        elementos.forEach(PalabraFrase::limpiar);
        palabrasAsignadas.clear();
    }

    // ============================================
    // Métodos auxiliares
    // ============================================

    private String generarClave(PalabraFlexion<?> palabra) {
        return palabra.getSloleksId() + ":" + palabra.getId();
    }

    // ============================================
    // Métodos abstractos / de identidad
    // ============================================

    /**
     * Identificador único para BD y configuración.
     * Debe ser constante y único (ej: "VERBO_TRANSITIVO_ACUSATIVO").
     */
    public abstract String getIdentificador();

    /**
     * Nombre para mostrar en la UI de configuración.
     */
    public abstract String getNombreMostrar();

    /**
     * Nombre descriptivo para logging.
     */
    public String getNombre() {
        return getNombreMostrar();
    }

    /**
     * Obtiene el nivel de dificultad de esta frase.
     * Si no tiene la anotación {@link DificultadFrase}, devuelve PRINCIPIANTE por defecto.
     */
    public NivelDificultad getDificultad() {
        DificultadFrase anotacion = this.getClass().getAnnotation(DificultadFrase.class);
        if (anotacion == null) {
            log.warn("La frase '{}' no tiene anotación @DificultadFrase, asignando PRINCIPIANTE por defecto",
                    getNombre());
        }
        return anotacion != null ? anotacion.value() : NivelDificultad.PRINCIPIANTE;
    }
}




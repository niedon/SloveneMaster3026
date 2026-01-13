package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.enums.TipoPalabra;
import com.bcadaval.esloveno.repo.*;
import com.bcadaval.esloveno.services.palabra.PalabraService;
import com.bcadaval.esloveno.services.xml.XmlParseService;
import com.bcadaval.esloveno.services.xml.XmlParserException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para cargar las palabras iniciales cuando la base de datos
 * se crea por primera vez.
 */
@Log4j2
@Service
public class DatosInicialesService {

    @Autowired
    private XmlParseService xmlParseService;

    @Lazy
    @Autowired
    private PalabraService palabraService;

    @Autowired
    private PronombreRepo pronombreRepo;

    @Autowired
    private NumeralRepo numeralRepo;

    @Autowired
    private VerboRepo verboRepo;

    @Autowired
    private AdjetivoRepo adjetivoRepo;

    @Autowired
    private SustantivoRepo sustantivoRepo;


    /**
     * Palabras iniciales a cargar por tipo
     */
    private static final List<String> PRONOMBRES_INICIALES = List.of("jaz", "ti", "on");
    private static final List<String> NUMERALES_INICIALES = List.of("en", "dva", "trije");
    private static final List<String> VERBOS_INICIALES = List.of("gledati");
    private static final List<String> ADJETIVOS_INICIALES = List.of("velik");
    private static final List<String> SUSTANTIVOS_INICIALES = List.of("pes");

    /**
     * Verifica si la base de datos tiene datos y, si no, carga las palabras iniciales.
     * Este método debe llamarse después de que la BD y los XMLs estén listos.
     */
    @Transactional
    public void cargarDatosInicialesSiNecesario() {
        cargarDatosInicialesSiNecesario(null, null);
    }

    /**
     * Verifica si la base de datos tiene datos y, si no, carga las palabras iniciales.
     * Versión con callbacks para actualizar progreso y mensaje.
     *
     * @param progressCallback Callback para actualizar el progreso (0-100)
     * @param messageCallback Callback para actualizar el mensaje
     */
    @Transactional
    public void cargarDatosInicialesSiNecesario(
            java.util.function.IntConsumer progressCallback,
            java.util.function.Consumer<String> messageCallback) {

        // Verificar primero si ya hay datos (optimización)
        if (hayDatosEnBD()) {
            log.debug("La base de datos ya tiene datos, omitiendo carga inicial");
            return;
        }

        log.info("Base de datos vacía detectada, cargando palabras iniciales...");

        if (messageCallback != null) {
            messageCallback.accept("Cargando palabras iniciales...");
        }

        int totalPalabras = PRONOMBRES_INICIALES.size() + NUMERALES_INICIALES.size() +
                           VERBOS_INICIALES.size() + ADJETIVOS_INICIALES.size() + SUSTANTIVOS_INICIALES.size();
        int cargadas = 0;
        int errores = 0;

        // Cargar pronombres
        for (String palabra : PRONOMBRES_INICIALES) {
            if (messageCallback != null) {
                messageCallback.accept("Cargando pronombre: " + palabra);
            }
            if (cargarPalabra(palabra, TipoPalabra.PRONOMBRE)) {
                cargadas++;
            } else {
                errores++;
            }
            if (progressCallback != null) {
                progressCallback.accept(90 + (cargadas * 10 / totalPalabras));
            }
        }

        // Cargar numerales
        for (String palabra : NUMERALES_INICIALES) {
            if (messageCallback != null) {
                messageCallback.accept("Cargando numeral: " + palabra);
            }
            if (cargarPalabra(palabra, TipoPalabra.NUMERAL)) {
                cargadas++;
            } else {
                errores++;
            }
            if (progressCallback != null) {
                progressCallback.accept(90 + (cargadas * 10 / totalPalabras));
            }
        }

        // Cargar verbos
        for (String palabra : VERBOS_INICIALES) {
            if (messageCallback != null) {
                messageCallback.accept("Cargando verbo: " + palabra);
            }
            if (cargarPalabra(palabra, TipoPalabra.VERBO)) {
                cargadas++;
            } else {
                errores++;
            }
            if (progressCallback != null) {
                progressCallback.accept(90 + (cargadas * 10 / totalPalabras));
            }
        }

        // Cargar adjetivos
        for (String palabra : ADJETIVOS_INICIALES) {
            if (messageCallback != null) {
                messageCallback.accept("Cargando adjetivo: " + palabra);
            }
            if (cargarPalabra(palabra, TipoPalabra.ADJETIVO)) {
                cargadas++;
            } else {
                errores++;
            }
            if (progressCallback != null) {
                progressCallback.accept(90 + (cargadas * 10 / totalPalabras));
            }
        }

        // Cargar sustantivos
        for (String palabra : SUSTANTIVOS_INICIALES) {
            if (messageCallback != null) {
                messageCallback.accept("Cargando sustantivo: " + palabra);
            }
            if (cargarPalabra(palabra, TipoPalabra.SUSTANTIVO)) {
                cargadas++;
            } else {
                errores++;
            }
            if (progressCallback != null) {
                progressCallback.accept(90 + (cargadas * 10 / totalPalabras));
            }
        }

        log.info("Carga de datos iniciales completada: {} palabras cargadas, {} errores", cargadas, errores);
    }

    /**
     * Verifica si hay algún dato en la base de datos
     */
    public boolean hayDatosEnBD() {
        return pronombreRepo.count() > 0 ||
               numeralRepo.count() > 0 ||
               verboRepo.count() > 0 ||
               adjetivoRepo.count() > 0 ||
               sustantivoRepo.count() > 0;
    }

    /**
     * Carga una palabra específica del tipo indicado
     */
    private boolean cargarPalabra(String lema, TipoPalabra tipoEsperado) {
        try {
            log.debug("Cargando {}: {}", tipoEsperado.getNombreEspanol(), lema);

            // Buscar todas las entradas para esta palabra
            var resultados = xmlParseService.buscarTodas(lema);

            // Filtrar por el tipo esperado
            var resultadoFiltrado = resultados.stream()
                .filter(r -> r.getTipo().equals(tipoEsperado.getXmlCode()))
                .findFirst();

            if (resultadoFiltrado.isEmpty()) {
                log.warn("No se encontró {} '{}' en los XMLs", tipoEsperado.getNombreEspanol(), lema);
                return false;
            }

            // Parsear y guardar
            Palabra<?> palabra = xmlParseService.parsearDesdeXml(resultadoFiltrado.get().getXmlContent());
            palabraService.saveWordAndConjugations(palabra);

            log.info("Cargada palabra {}: {} con {} flexiones",
                tipoEsperado.getNombreEspanol(), lema,
                palabra.getListaFlexiones() != null ? palabra.getListaFlexiones().size() : 0);

            return true;

        } catch (XmlParserException e) {
            log.error("Error cargando {}: {} - {}", tipoEsperado.getNombreEspanol(), lema, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error inesperado cargando {}: {}", lema, e.getMessage(), e);
            return false;
        }
    }
}


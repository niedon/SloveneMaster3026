package com.bcadaval.esloveno.rest;

import com.bcadaval.esloveno.beans.enums.TipoPalabra;
import com.bcadaval.esloveno.rest.dto.FlexionDetalleDTO;
import com.bcadaval.esloveno.rest.dto.PalabraGuardadaDTO;
import com.bcadaval.esloveno.services.BuscarPalabrasService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Controlador para buscar y explorar palabras guardadas en el sistema.
 * Proporciona una página de búsqueda con filtros y un panel de detalle
 * de flexiones con estadísticas SRS.
 */
@Log4j2
@Controller
public class BuscarPalabrasController {

    @Autowired
    private BuscarPalabrasService buscarPalabrasService;

    /**
     * Muestra la página de búsqueda de palabras
     */
    @SuppressWarnings("SameReturnValue")
    @GetMapping("/buscarPalabras")
    public String mostrarPaginaBuscar() {
        log.debug("Accediendo a la página de buscar palabras");
        return "buscarPalabras";
    }

    /**
     * Busca palabras guardadas por texto (en principal o significado).
     * Si el texto está vacío, devuelve todas las palabras.
     *
     * @param texto Texto a buscar
     * @return Lista de palabras encontradas
     */
    @GetMapping("/api/buscarPalabrasGuardadas")
    @ResponseBody
    public List<PalabraGuardadaDTO> buscarPalabras(@RequestParam(defaultValue = "") String texto) {
        log.debug("Buscando palabras guardadas: '{}'", texto);
        return buscarPalabrasService.buscarPalabras(texto);
    }

    /**
     * Obtiene el detalle de flexiones de una palabra con estadísticas SRS.
     *
     * @param sloleksId ID de la palabra
     * @param tipo      Tipo de palabra (xmlCode o nombre del enum)
     * @return Lista de flexiones con detalle
     */
    @GetMapping("/api/detalleFlexiones")
    @ResponseBody
    public List<FlexionDetalleDTO> obtenerDetalleFlexiones(
            @RequestParam String sloleksId,
            @RequestParam String tipo) {

        log.debug("Obteniendo detalle de flexiones: sloleksId={}, tipo={}", sloleksId, tipo);

        TipoPalabra tipoPalabra = TipoPalabra.fromXmlCode(tipo);
        if (tipoPalabra == null) {
            try {
                tipoPalabra = TipoPalabra.valueOf(tipo);
            } catch (IllegalArgumentException e) {
                log.error("Tipo de palabra no reconocido: {}", tipo);
                return List.of();
            }
        }

        return buscarPalabrasService.obtenerFlexiones(sloleksId, tipoPalabra);
    }
}


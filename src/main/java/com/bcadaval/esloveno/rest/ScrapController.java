package com.bcadaval.esloveno.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.bcadaval.esloveno.beans.palabra.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.rest.dto.BusquedaMultipleResponse;
import com.bcadaval.esloveno.rest.dto.BusquedaPalabraResponse;
import com.bcadaval.esloveno.services.palabra.PalabraService;
import com.bcadaval.esloveno.services.xml.XmlParseService;
import com.bcadaval.esloveno.services.xml.XmlParseService.ResultadoBusqueda;

import lombok.extern.log4j.Log4j2;

/**
 * Controlador para buscar y añadir palabras desde XML al diccionario
 */
@Log4j2
@Controller
public class ScrapController {

	@Autowired
    private PalabraService palabraService;

	@Autowired
	private XmlParseService xmlParseService;

	/** Cache temporal para almacenar resultados de búsqueda pendientes de guardar */
	private final ConcurrentHashMap<String, List<ResultadoBusqueda>> resultadosCache = new ConcurrentHashMap<>();
	private final AtomicInteger sessionCounter = new AtomicInteger(0);

	/**
	 * Muestra la página para añadir palabras al diccionario
	 */
	@GetMapping("/anadirPalabras")
	public String mostrarPaginaAnadirPalabras() {
		log.debug("Accediendo a la página de añadir palabras");
		return "anadirPalabras";
	}

	/**
	 * Busca todas las entradas de una palabra en los XMLs
	 * @param word Palabra a buscar
	 * @return Lista de resultados con tipos
	 */
	@GetMapping("/api/buscarTodas")
	@ResponseBody
	public BusquedaMultipleResponse buscarTodas(@RequestParam String word) {
		log.info("Buscando todas las entradas para: {}", word);

		try {
			List<ResultadoBusqueda> resultados = xmlParseService.buscarTodas(word);

			if (resultados.isEmpty()) {
				return BusquedaMultipleResponse.builder()
						.exito(false)
						.mensaje("No se encontró la palabra '" + word + "' en el diccionario")
						.palabra(word)
						.totalResultados(0)
						.resultados(new ArrayList<>())
						.build();
			}

			// Guardar en cache para poder guardar después
			String sessionId = String.valueOf(sessionCounter.incrementAndGet());
			resultadosCache.put(sessionId, resultados);

			// Construir lista de items
			List<BusquedaMultipleResponse.ResultadoItem> items = new ArrayList<>();
			for (int i = 0; i < resultados.size(); i++) {
				ResultadoBusqueda r = resultados.get(i);
				items.add(BusquedaMultipleResponse.ResultadoItem.builder()
						.lema(r.getLema())
						.tipo(r.getTipo())
						.tipoEspanol(r.getTipoEspanol())
						.soportado(r.isSoportado())
						.indice(i)
						.build());
			}

			log.info("Encontradas {} entradas para '{}', sessionId: {}", resultados.size(), word, sessionId);

			return BusquedaMultipleResponse.builder()
					.exito(true)
					.mensaje("Se encontraron " + resultados.size() + " entrada(s)")
					.palabra(word + "|" + sessionId) // Incluir sessionId para guardar después
					.totalResultados(resultados.size())
					.resultados(items)
					.build();

		} catch (Exception e) {
			log.error("Error buscando palabra '{}': {}", word, e.getMessage(), e);
			return BusquedaMultipleResponse.builder()
					.exito(false)
					.mensaje("Error al buscar: " + e.getMessage())
					.palabra(word)
					.totalResultados(0)
					.resultados(new ArrayList<>())
					.build();
		}
	}

	/**
	 * Guarda una palabra específica dado su índice en los resultados de búsqueda
	 * @param sessionId ID de la sesión de búsqueda
	 * @param indice Índice del resultado a guardar
	 * @return Resultado de la operación
	 */
	@PostMapping("/api/guardarPalabra")
	@ResponseBody
	public BusquedaPalabraResponse guardarPalabra(@RequestParam String sessionId, @RequestParam int indice) {
		log.info("Guardando palabra - sessionId: {}, indice: {}", sessionId, indice);

		try {
			List<ResultadoBusqueda> resultados = resultadosCache.get(sessionId);

			if (resultados == null) {
				return BusquedaPalabraResponse.builder()
						.exito(false)
						.mensaje("Sesión de búsqueda expirada. Por favor, busca de nuevo.")
						.build();
			}

			if (indice < 0 || indice >= resultados.size()) {
				return BusquedaPalabraResponse.builder()
						.exito(false)
						.mensaje("Índice de resultado inválido")
						.build();
			}

			ResultadoBusqueda resultado = resultados.get(indice);

			if (!resultado.isSoportado()) {
				return BusquedaPalabraResponse.builder()
						.exito(false)
						.mensaje("El tipo '" + resultado.getTipoEspanol() + "' no está soportado")
						.build();
			}

			// Parsear y guardar la palabra
			Palabra<?> palabra = xmlParseService.parsearDesdeXml(resultado.getXmlContent());
			palabraService.saveWordAndConjugations(palabra);

			// Limpiar cache de esta sesión
			resultadosCache.remove(sessionId);

			log.info("Palabra guardada: {} ({})", resultado.getLema(), resultado.getTipoEspanol());

			return BusquedaPalabraResponse.builder()
					.exito(true)
					.mensaje("La palabra '" + resultado.getLema() + "' (" + resultado.getTipoEspanol() + ") se ha guardado correctamente")
					.palabra(resultado.getLema())
					.tipoPalabra(resultado.getTipoEspanol())
					.build();

		} catch (Exception e) {
			log.error("Error guardando palabra: {}", e.getMessage(), e);
			return BusquedaPalabraResponse.builder()
					.exito(false)
					.mensaje("Error al guardar: " + e.getMessage())
					.build();
		}
	}

	/**
	 * Busca una palabra de forma asíncrona en los archivos XML y la guarda en la base de datos
	 * @deprecated Usar /api/buscarTodas y /api/guardarPalabra en su lugar
	 */
	@Deprecated
	@GetMapping("/api/buscarPalabra")
	@ResponseBody
	public BusquedaPalabraResponse buscarPalabraAsync(@RequestParam String word) {
		log.info("Buscando palabra (legacy): {}", word);

		try {
			Palabra<?> palabra = xmlParseService.parsearDesdeXml(word);

			if (palabra == null) {
				return BusquedaPalabraResponse.builder()
						.exito(false)
						.mensaje("No se encontró la palabra '" + word + "' en el diccionario")
						.palabra(word)
						.build();
			}

			palabraService.saveWordAndConjugations(palabra);
			String tipoPalabra = determinarTipoPalabra(palabra);

			return BusquedaPalabraResponse.builder()
					.exito(true)
					.mensaje("La palabra '" + word + "' se ha guardado correctamente")
					.palabra(word)
					.tipoPalabra(tipoPalabra)
					.build();

		} catch (Exception e) {
			log.error("Error al procesar la palabra '{}': {}", word, e.getMessage(), e);
			return BusquedaPalabraResponse.builder()
					.exito(false)
					.mensaje("Error al procesar la palabra: " + e.getMessage())
					.palabra(word)
					.build();
		}
	}

	/**
	 * Endpoint legacy para mantener compatibilidad
	 * @deprecated Usar /api/buscarTodas y /api/guardarPalabra
	 */
	@Deprecated
	@GetMapping("/scrapWordFromXML")
	@ResponseBody
	public String scrapWordFromXML(@RequestParam String word) {
		log.warn("Usando endpoint legacy /scrapWordFromXML para palabra: {}", word);

		String resultado = "Se ha guardado la palabra y sus flexiones";
		try {
			palabraService.saveWordAndConjugations(xmlParseService.parsearDesdeXml(word));
		} catch (Exception e) {
			log.error("Ha ocurrido un error procesando la palabra: " + e.getMessage(), e);
			resultado = "No se ha podido procesar la palabra: " + e.getMessage();
		}
		
		return resultado;
	}
	
	private String determinarTipoPalabra(Palabra<?> palabra) {
        return switch (palabra) {
            case Verbo verbo -> "Verbo";
            case Sustantivo sustantivo -> "Sustantivo";
            case Adjetivo adjetivo -> "Adjetivo";
            case Pronombre pronombre -> "Pronombre";
            case Numeral numeral -> "Numeral";
            default -> palabra.getClass().getSimpleName();
        };
	}
}

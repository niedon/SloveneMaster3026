package com.bcadaval.esloveno.rest;

import java.util.ArrayList;
import java.util.List;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.services.EstructuraFraseService;
import com.bcadaval.esloveno.services.RepeticionEspaciadaService;
import com.bcadaval.esloveno.services.VariablesService;
import com.bcadaval.esloveno.structures.DatoVisualizacion;
import com.bcadaval.esloveno.structures.EstructuraFrase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.log4j.Log4j2;

/**
 * Controlador para obtener palabras para estudiar.
 * Integra el sistema de repetición espaciada para mostrar tarjetas disponibles.
 * Usa el sistema de EstructuraFrase para construir frases con patrones.
 */
@Log4j2
@Controller
public class WordsController {

	@Autowired
	private RepeticionEspaciadaService repeticionEspaciadaService;

	@Autowired
	private VariablesService variablesService;

	@Autowired
	private EstructuraFraseService estructuraFraseService;

	/**
	 * Obtiene palabras para practicar basándose en el sistema de repetición espaciada.
	 * Muestra el número de tarjetas disponibles y las tarjetas a estudiar.
	 */
	@GetMapping("/getWords")
	public String getWords(Model model) {
		// Contar tarjetas disponibles
		int tarjetasDisponibles = repeticionEspaciadaService.contarTarjetasDisponibles();
		int tarjetasNuevas = repeticionEspaciadaService.contarTarjetasNuevas();
		int maxRevision = variablesService.getMaxTarjetasRevisionDia();

		log.info("Tarjetas disponibles: {}, Nuevas: {}", tarjetasDisponibles, tarjetasNuevas);

		// Pasar contador al modelo
		model.addAttribute("tarjetasDisponibles", tarjetasDisponibles);
		model.addAttribute("tarjetasNuevas", tarjetasNuevas);

		// Obtener tarjetas para estudiar (ya ordenadas por prioridad SRS)
		List<PalabraFlexion<?>> tarjetas = repeticionEspaciadaService.obtenerTarjetasDisponibles(maxRevision);

		List<DatoVisualizacion> datos;
		if (tarjetas.isEmpty()) {
			log.warn("No hay tarjetas disponibles para estudiar");
			datos = new ArrayList<>();
		} else {
			datos = construirFrase(tarjetas);
		}

		// Pasar al modelo
		model.addAttribute("datos", datos);

		return "estudioPalabras";
	}

	/**
	 * Construye una frase buscando la primera estructura activa que se complete
	 * con las palabras disponibles (ya ordenadas por prioridad SRS).
	 *
	 * @param tarjetas Lista de palabras disponibles
	 * @return Lista de DatoVisualizacion para el JSP
	 */
	private List<DatoVisualizacion> construirFrase(List<PalabraFlexion<?>> tarjetas) {
		// Obtener solo las estructuras activas
		List<EstructuraFrase> estructuras = estructuraFraseService.getEstructurasActivas();

		if (estructuras.isEmpty()) {
			log.warn("No hay estructuras de frase activas");
			return new ArrayList<>();
		}

		// Limpiar slots antes de usar (singleton)
		estructuras.forEach(EstructuraFrase::limpiar);

		// Iterar sobre palabras (ya ordenadas por prioridad SRS)
		for (int i = 0; i < tarjetas.size(); i++) {
			PalabraFlexion palabra = tarjetas.get(i);

			for (EstructuraFrase estructura : estructuras) {
				estructura.intentarAsignar(palabra, i);

				// Primera estructura completa gana
				if (estructura.estaCompleta()) {
					log.info("Estructura '{}' completada", estructura.getNombre());
					return estructura.construirDatosVisualizacion();
				}
			}
		}

		// Ninguna estructura se completó
		log.warn("Ninguna estructura de frase se completó con las {} tarjetas disponibles", tarjetas.size());
		return new ArrayList<>();
	}
}

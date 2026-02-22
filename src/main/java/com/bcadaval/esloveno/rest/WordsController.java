package com.bcadaval.esloveno.rest;

import java.util.ArrayList;
import java.util.Comparator;
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
		int maxRevision = variablesService.getMaxTarjetasRevisionDia();

		// Obtener tarjetas listas para estudiar (proximaRevision <= ahora)
		List<PalabraFlexion<?>> tarjetas = repeticionEspaciadaService.obtenerTarjetasDisponibles(maxRevision);

		log.info("Tarjetas disponibles: {}", tarjetas.size());

		// Pasar contadores al modelo
		model.addAttribute("tarjetasDisponibles", tarjetas.size());

		List<DatoVisualizacion> datos;
		if (tarjetas.isEmpty()) {
			log.warn("No hay tarjetas disponibles para estudiar");
			datos = new ArrayList<>();
		} else {
			datos = construirFrase(tarjetas);
		}

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

		// Limpiar slots antes de usar (singleton)
		estructuras.forEach(EstructuraFrase::limpiar);

		// Intentar asignar palabras a todas las estructuras
		tarjetas.forEach(palabra -> {
			estructuras.forEach(estructura -> {
				estructura.intentarAsignar(palabra);
			});
		});

		// Calcular puntuación para estructuras completas (media de usos de sus palabras)
		// Menor puntuación = mejor (prioriza palabras menos asignables)
		List<EstructuraFrase> candidatas = estructuras.stream()
				.filter(EstructuraFrase::estaCompleta)
				.sorted(Comparator.comparing(EstructuraFrase::calcularMediaInstant))
				.toList();

		// Log de puntuaciones para debug
		candidatas.forEach(el -> log.info("Estructura candidata '{}': puntuación = {}",
				el.getNombreMostrar(), el.calcularMediaInstant()));

		// Intentar construir cada candidata; descartar si algún generador falla
		for (EstructuraFrase candidata : candidatas) {
			log.info("Intentando construir estructura '{}'...", candidata.getNombreMostrar());
			List<DatoVisualizacion> resultado = candidata.construirDatosVisualizacion();
			if (resultado != null) {
				log.info("Estructura seleccionada: '{}' con puntuación {}",
						candidata.getNombreMostrar(), candidata.calcularMediaInstant());
				return resultado;
			}
			// Si devuelve null, el log de error ya se emitió en construirDatosVisualizacion
		}


		// Ninguna estructura pudo construirse
		if (candidatas.isEmpty()) {
			log.warn("Ninguna estructura de frase se completó con las {} tarjetas disponibles", tarjetas.size());
		} else {
			log.warn("Se completaron {} estructura(s) pero ninguna pudo generar todos sus componentes", candidatas.size());
		}
		log.info("=================== Tarjetas no usadas ==================");
		for (PalabraFlexion<?> tarjeta : tarjetas) {
			log.info(" - {}", tarjeta);
		}
		log.info("=============== Fin de tarjetas no usadas ===============");
		return new ArrayList<>();
	}
}

package com.bcadaval.esloveno.rest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.services.FraseService;
import com.bcadaval.esloveno.services.RepeticionEspaciadaService;
import com.bcadaval.esloveno.services.VariablesService;
import com.bcadaval.esloveno.structures.DatoVisualizacion;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.log4j.Log4j2;

/**
 * Controlador para obtener palabras para estudiar.
 * Integra el sistema de repetición espaciada para mostrar tarjetas disponibles.
 * Usa el nuevo sistema de {@link Frase} para construir frases con patrones.
 */
@Log4j2
@Controller
public class WordsController {

	@Autowired
	private RepeticionEspaciadaService repeticionEspaciadaService;

	@Autowired
	private VariablesService variablesService;

	@Autowired
	private FraseService fraseService;

	/**
	 * Obtiene palabras para practicar basándose en el sistema de repetición espaciada.
	 * <p>
	 * Flujo:
	 * <ol>
	 *   <li>Obtiene tarjetas SRS disponibles (ordenadas y desplazadas)</li>
	 *   <li>Obtiene frases activas y válidas</li>
	 *   <li>Bucle multi-pasada: cada palabra pasa por cada frase hasta que una pasada no produzca asignaciones</li>
	 *   <li>Filtra frases con todos los huecos obligatorios rellenos</li>
	 *   <li>Elige la frase con media de proximaRevision más antigua</li>
	 *   <li>Genera los huecos de apoyo/opcionales no rellenos y envía al JSP</li>
	 * </ol>
	 */
	@SuppressWarnings("SameReturnValue")
	@GetMapping("/getWords")
	public String getWords(Model model) {
		int maxRevision = variablesService.getMaxTarjetasRevisionDia();

		// Obtener tarjetas listas para estudiar con el nuevo sistema
		List<PalabraFlexion<?>> tarjetas = repeticionEspaciadaService.obtenerTarjetasDisponiblesNuevo(maxRevision);

		long tarjetasNuevas = tarjetas.stream().filter(t -> t.getProximaRevision() == null).count();
		long tarjetasRevision = tarjetas.size() - tarjetasNuevas;

		log.info("Tarjetas disponibles: {} (revisión: {}, nuevas: {})", tarjetas.size(), tarjetasRevision, tarjetasNuevas);
		model.addAttribute("tarjetasDisponibles", tarjetasRevision);
		model.addAttribute("tarjetasNuevas", tarjetasNuevas);

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
	 * Construye una frase usando el bucle de asignación multi-pasada.
	 * <p>
	 * Algoritmo:
	 * <ol>
	 *   <li>Obtener frases activas y válidas, limpiar su estado</li>
	 *   <li><strong>Bucle de pasadas:</strong> para cada palabra, intentar asignar a cada frase.
	 *       Repetir hasta que una pasada completa no produzca ninguna asignación nueva.</li>
	 *   <li>Filtrar frases completas (huecos obligatorios rellenos)</li>
	 *   <li>Elegir la frase con media de proximaRevision más antigua (más "atrasada")</li>
	 *   <li>Generar apoyos/opcionales y construir datos de visualización</li>
	 * </ol>
	 *
	 * @param tarjetas Lista de palabras disponibles (ya ordenadas y desplazadas)
	 * @return Lista de DatoVisualizacion para el JSP
	 */
	private List<DatoVisualizacion> construirFrase(List<PalabraFlexion<?>> tarjetas) {
		List<Frase> frases = fraseService.getFrasesActivasYValidas();

		// Limpiar estado de frases (singleton)
		frases.forEach(Frase::limpiar);

		// Bucle de asignación multi-pasada
		boolean huboCambio;
		int pasada = 0;
		do {
			huboCambio = false;
			pasada++;
			for (PalabraFlexion<?> palabra : tarjetas) {
				for (Frase frase : frases) {
					if (frase.intentarAsignar(palabra)) {
						huboCambio = true;
					}
				}
			}
			log.debug("Pasada {}: huboCambio={}", pasada, huboCambio);
		} while (huboCambio);

		log.info("Asignación completada en {} pasadas", pasada);

		// Filtrar frases completas y ordenar por media de proximaRevision (más antigua primero)
		List<Frase> candidatas = frases.stream()
				.filter(Frase::estaCompleta)
				.sorted(Comparator.comparing(Frase::calcularMediaInstant))
				.toList();

		candidatas.forEach(f -> log.info("Frase candidata '{}': media = {}",
				f.getNombreMostrar(), f.calcularMediaInstant()));

		// Intentar construir cada candidata (genera apoyos/opcionales)
		for (Frase candidata : candidatas) {
			log.info("Intentando construir frase '{}'...", candidata.getNombreMostrar());
			List<DatoVisualizacion> resultado = candidata.construirDatosVisualizacion(repeticionEspaciadaService);
			if (resultado != null) {
				log.info("Frase seleccionada: '{}' con media {}",
						candidata.getNombreMostrar(), candidata.calcularMediaInstant());

				// Inicializar campos SRS de tarjetas nuevas asignadas por criterio a esta frase
				candidata.getElementos().stream()
						.filter(PalabraFrase::participaEnSRS)
						.map(PalabraFrase::getPalabraAsignada)
						.filter(Objects::nonNull)
						.filter(p -> p.getProximaRevision() == null)
						.forEach(repeticionEspaciadaService::inicializarTarjetaNueva);

				return resultado;
			}
		}

		// Ninguna frase pudo construirse
		if (candidatas.isEmpty()) {
			log.warn("Ninguna frase se completó con las {} tarjetas disponibles", tarjetas.size());
		} else {
			log.warn("Se completaron {} frase(s) pero ninguna pudo generar todos sus componentes", candidatas.size());
		}
		log.info("=================== Tarjetas no usadas ==================");
		for (PalabraFlexion<?> tarjeta : tarjetas) {
			log.info(" - {}", tarjeta);
		}
		log.info("=============== Fin de tarjetas no usadas ===============");
		return new ArrayList<>();
	}
}

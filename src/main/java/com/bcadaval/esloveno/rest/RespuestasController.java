package com.bcadaval.esloveno.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.TreeMap;

import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.services.RepeticionEspaciadaService;

import lombok.extern.log4j.Log4j2;

/**
 * Controlador para procesar las respuestas del formulario de palabras.
 * Integra el sistema de repetición espaciada (SRS) para actualizar
 * el progreso de aprendizaje de cada tarjeta.
 * <p>
 * Recibe respuestas de forma: tipo_INDEX, id_INDEX, valor_INDEX
 * Donde INDEX es el índice del elemento en la lista del formulario
 * tipo es 'v', 's', 'a' o 'p' (verbo, sustantivo, adjetivo, pronombre)
 * id es el identificador numérico de la flexión en su tabla
 * Y valor es "arriba" (recordó) o "abajo" (no recordó)
 */
@Log4j2
@Controller
public class RespuestasController {

	@Autowired
	private VerboFlexionRepo verboFlexionRepo;

	@Autowired
	private SustantivoFlexionRepo sustantivoFlexionRepo;

	@Autowired
	private AdjetivoFlexionRepo adjetivoFlexionRepo;

	@Autowired
	private RepeticionEspaciadaService repeticionEspaciadaService;

	@PostMapping("/enviarRespuestas")
	public String enviarRespuestas(@RequestParam Map<String, String> params) {
		try {
			log.info("\n=== RESPUESTAS RECIBIDAS ===");

			// Agrupar los datos por índice (usar TreeMap para mantener orden)
			Map<Integer, Map<String, String>> palabrasPorIndice = new TreeMap<>();

			for (Map.Entry<String, String> entry : params.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();

				// Las claves son del formato: tipo_INDEX, id_INDEX, valor_INDEX
				if (key.startsWith("tipo_") || key.startsWith("id_") || key.startsWith("valor_")) {
					String[] partes = key.split("_", 2);
					if (partes.length == 2) {
						String campo = partes[0]; // "tipo", "id" o "valor"
						Integer indice = Integer.parseInt(partes[1]); // El índice

						// Crear el mapa para este índice si no existe
						palabrasPorIndice.putIfAbsent(indice, new TreeMap<>());
						palabrasPorIndice.get(indice).put(campo, value);
					}
				}
			}

			// Procesar cada palabra y actualizar el SRS
			for (Map.Entry<Integer, Map<String, String>> entry : palabrasPorIndice.entrySet()) {
				Integer indice = entry.getKey();
				Map<String, String> datos = entry.getValue();

				String tipoStr = datos.get("tipo");
				String idStr = datos.get("id");
				String valoracion = datos.get("valor");

				if (tipoStr == null || idStr == null || valoracion == null) {
					log.warn("Datos incompletos para índice {}: tipo={}, id={}, valoracion={}",
							indice, tipoStr, idStr, valoracion);
					continue;
				}

				Integer id = Integer.parseInt(idStr);
				boolean recordo = "arriba".equals(valoracion);

				log.info("Procesando: tipo={}, id={}, recordó={}", tipoStr, id, recordo);

				// Recuperar la entidad y actualizar el SRS
				switch (tipoStr) {
					case "v": // Verbo
						verboFlexionRepo.findById(id).ifPresentOrElse(
							verbo -> {
								log.info("VerboFlexion: {} - Recordó: {}", verbo.getFlexion(), recordo);
								repeticionEspaciadaService.procesarRespuesta(verbo, recordo);
							},
							() -> log.warn("VerboFlexion con ID {} no encontrado", id)
						);
						break;

					case "s": // Sustantivo
						sustantivoFlexionRepo.findById(id).ifPresentOrElse(
							sustantivo -> {
								log.info("SustantivoFlexion: {} - Recordó: {}", sustantivo.getFlexion(), recordo);
								repeticionEspaciadaService.procesarRespuesta(sustantivo, recordo);
							},
							() -> log.warn("SustantivoFlexion con ID {} no encontrado", id)
						);
						break;

					case "a": // Adjetivo
						adjetivoFlexionRepo.findById(id).ifPresentOrElse(
							adjetivo -> {
								log.info("AdjetivoFlexion: {} - Recordó: {}", adjetivo.getFlexion(), recordo);
								repeticionEspaciadaService.procesarRespuesta(adjetivo, recordo);
							},
							() -> log.warn("AdjetivoFlexion con ID {} no encontrado", id)
						);
						break;

					case "p": // Pronombre (sin SRS por ahora)
						log.info("Pronombre (tipo 'p') - ID: {} - No tiene SRS", id);
						break;

					default:
						log.warn("Tipo de palabra desconocido: {}", tipoStr);
				}
			}

			log.info("=== FIN PROCESAMIENTO ===\n");

		} catch (Exception e) {
			log.error("Error procesando respuestas: {}", e.getMessage(), e);
		}

		// Redirigir de vuelta al formulario
		return "redirect:/getWords";
	}
}

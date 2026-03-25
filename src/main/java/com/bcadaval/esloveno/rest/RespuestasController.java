package com.bcadaval.esloveno.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.TreeMap;

import com.bcadaval.esloveno.beans.enums.TipoPalabra;
import com.bcadaval.esloveno.services.RepeticionEspaciadaService;

import lombok.extern.log4j.Log4j2;

/**
 * Controlador para procesar las respuestas del formulario de palabras.
 * Integra el sistema de repetición espaciada (SRS) para actualizar
 * el progreso de aprendizaje de cada tarjeta.
 * <p>
 * Recibe respuestas de forma: tipo_INDEX, id_INDEX, valor_INDEX
 * Donde INDEX es el índice del elemento en la lista del formulario
 * tipo es el xmlCode de TipoPalabra
 * id es el identificador numérico de la flexión en su tabla
 * Y valor es "arriba" (recordó) o "abajo" (no recordó)
 */
@Log4j2
@Controller
public class RespuestasController {

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

            // Calcular el número de palabras evaluadas y repartir el tiempo si procede
            int numPalabrasConRespuesta = palabrasPorIndice.size();
            Integer segundosEnResponder = null;

            if (params.containsKey("tiempoTotalSegundos")) {
                try {
                    int tiempoTotal = Integer.parseInt(params.get("tiempoTotalSegundos"));
                    if (tiempoTotal <= 120 && numPalabrasConRespuesta > 0) {
                        segundosEnResponder = tiempoTotal / numPalabrasConRespuesta;
                        log.info("Tiempo de respuesta válido. Total: {}s, Palabras: {}, Segundos/Palabra: {}s",
                                tiempoTotal, numPalabrasConRespuesta, segundosEnResponder);
                    } else if (tiempoTotal > 120) {
                        log.info("Tiempo de respuesta excedido (>120s): {}s. Se descarta para el promedio.", tiempoTotal);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Formato inválido para tiempoTotalSegundos: {}", params.get("tiempoTotalSegundos"));
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

				TipoPalabra tipo = TipoPalabra.fromXmlCode(tipoStr);
				if (tipo == null) {
					log.warn("Tipo de palabra desconocido: {}", tipoStr);
					continue;
				}

				Integer id = Integer.parseInt(idStr);
				boolean recordo = "arriba".equals(valoracion);

				log.info("Procesando: tipo={} ({}), id={}, recordó={}", tipo, tipoStr, id, recordo);

                // variable para ser usada dentro de la lambda
                final Integer sEnResponder = segundosEnResponder;

				repeticionEspaciadaService.findFlexionById(tipo, id).ifPresentOrElse(
					flexion -> {
						log.info("Flexion encontrada: {} ({}) - Recordó: {}",
								flexion.getFlexion(), flexion.getClass().getSimpleName(), recordo);
						repeticionEspaciadaService.procesarRespuesta(flexion, recordo, sEnResponder);
					},
					() -> log.warn("Flexion de tipo {} con ID {} no encontrada", tipo, id)
				);
			}

			log.info("=== FIN PROCESAMIENTO ===\n");

		} catch (NumberFormatException e) {
			log.error("Error parseando ID de palabra: {}", e.getMessage());
			// Continuar para no perder la sesión del usuario
		} catch (Exception e) {
			log.error("Error procesando respuestas: {}", e.getMessage(), e);
			// Continuar para no perder la sesión del usuario
		}

		// Redirigir de vuelta al formulario
		return "redirect:/getWords";
	}
}

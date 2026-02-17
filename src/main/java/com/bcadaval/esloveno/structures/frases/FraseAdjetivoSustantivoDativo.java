package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Grado;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.ElementoFrase;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.ExcluirDeFrases;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Estructura de frase: Adjetivo + Sustantivo en caso Dativo
 * <p>
 * Ejemplo: "dam lepemu otroku" (doy al niño bonito), "povem staremu prijatelju" (digo al viejo amigo)
 * <p>
 * Esta estructura practica la concordancia adjetivo-sustantivo en dativo,
 * fundamental para expresar destinatarios con descripción:
 * - Objeto indirecto: dam knjigo novemu učitelju (doy el libro al nuevo profesor)
 * - Dirección hacia: grem k staremu prijatelju (voy hacia el viejo amigo)
 * - Beneficiario: kupim darilo mladi sestri (compro un regalo para la hermana joven)
 * <p>
 * El dativo es esencial para verbos de comunicación y transferencia.
 * <p>
 * Elementos:
 * 1. NUMERAL (apoyo): indica el número gramatical
 * 2. ADJETIVO (slot): AdjetivoFlexion en DATIVO y grado POSITIVO
 * 3. SUSTANTIVO (apoyo): SustantivoFlexion que concuerda con el adjetivo
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseAdjetivoSustantivoDativo extends EstructuraFrase {

    @Getter
    private final String identificador = "ADJETIVO_SUSTANTIVO_DATIVO";
    @Getter
    private final String nombreMostrar = "Adjetivo + Sustantivo (DAT)";

    public FraseAdjetivoSustantivoDativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de adjetivo en dativo y grado positivo
        ElementoFrase<AdjetivoFlexion> adjetivo = ElementoFrase.<AdjetivoFlexion>builder()
                .nombre("ADJETIVO")
                .criterio(CriterioBusqueda.de(AdjetivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.DATIVO)
                        .con(CaracteristicaGramatical.GRADO, Grado.POSITIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        // Definir apoyo de numeral (depende del adjetivo)
        ElementoFrase<NumeralFlexion> numeral = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERAL")
                .generador(adjetivo, palabra -> numeralService.getNumeral(adjetivo.getPalabraAsignada()))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Definir apoyo de sustantivo (depende del adjetivo, debe coincidir en caso, género y número)
        ElementoFrase<SustantivoFlexion> sustantivo = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .generador(adjetivo, palabra -> sustantivoService.getSustantivo(adjetivo.getPalabraAsignada()))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Agregar en orden de visualización
        agregarElemento(numeral);
        agregarElemento(adjetivo);
        agregarElemento(sustantivo);
    }
}

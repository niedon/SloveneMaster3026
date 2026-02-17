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
 * Estructura de frase: Adjetivo + Sustantivo en caso Acusativo
 * <p>
 * Ejemplo: "vidim lepo knjigo" (veo el libro bonito), "imam velikega psa" (tengo un perro grande)
 * <p>
 * Esta estructura practica la concordancia adjetivo-sustantivo en acusativo,
 * que es el caso del objeto directo. Es fundamental porque:
 * - Los verbos transitivos requieren acusativo: imam + ACU
 * - El adjetivo debe concordar con el sustantivo en género, número Y caso
 * - El masculino animado singular tiene una forma especial (igual al genitivo)
 * <p>
 * Elementos:
 * 1. NUMERAL (apoyo): numeral que indica el número gramatical
 * 2. ADJETIVO (slot): AdjetivoFlexion con caso ACUSATIVO y grado POSITIVO
 * 3. SUSTANTIVO (apoyo): SustantivoFlexion que concuerda con el adjetivo
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseSustantivoAdjetivoAcusativo extends EstructuraFrase {

    @Getter
    private final String identificador = "SUSTANTIVO_ADJETIVO_ACUSATIVO";
    @Getter
    private final String nombreMostrar = "Adjetivo + Sustantivo (ACU)";

    public FraseSustantivoAdjetivoAcusativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de adjetivo en acusativo y grado positivo
        ElementoFrase<AdjetivoFlexion> adjetivo = ElementoFrase.<AdjetivoFlexion>builder()
                .nombre("ADJETIVO")
                .criterio(CriterioBusqueda.de(AdjetivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.ACUSATIVO)
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

        // Agregar en orden de visualización (adjetivo antes del sustantivo en esloveno)
        agregarElemento(numeral);
        agregarElemento(adjetivo);
        agregarElemento(sustantivo);
    }
}

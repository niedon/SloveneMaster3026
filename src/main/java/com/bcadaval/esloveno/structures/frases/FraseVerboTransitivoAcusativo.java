package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.ElementoFrase;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.extractores.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Estructura de frase: Pronombre + Verbo transitivo + Número + Sustantivo Acusativo
 * <p>
 * Ejemplo: "jaz vidim 1 knjigo" (yo veo 1 libro)
 * <p>
 * Elementos (en orden):
 * 1. PRONOMBRE (apoyo): generado a partir del verbo
 * 2. VERBO (opcional): VerboFlexion con transitividad TRANSITIVO y forma PRESENT.
 *    Si no hay verbos transitivos entre las tarjetas SRS, se genera uno aleatorio de la BD.
 *    En ese caso, el verbo no participará en SRS (sin botones 👍/👎 en el frontend).
 * 3. NUMERO (apoyo): numeral que concuerda con el sustantivo
 * 4. CD (slot): SustantivoFlexion con caso ACUSATIVO
 */
/**
 * @deprecated Sustituida por {@link com.bcadaval.esloveno.structures.frase.frases.FraseVerboTransitivoAcusativoNueva}
 */
@Deprecated
// @Component — Desactivado: usar la nueva implementación en el paquete frase
@DificultadFrase(NivelDificultad.ELEMENTAL)
public class FraseVerboTransitivoAcusativo extends EstructuraFrase {

    @Getter
    private final String identificador = "VERBO_TRANSITIVO_ACUSATIVO";
    @Getter
    private final String nombreMostrar = "Verbo (tr) + Sustantivo (ACU)";

    public FraseVerboTransitivoAcusativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de sustantivo en acusativo (obligatorio)
        ElementoFrase<SustantivoFlexion> cd = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("CD")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.ACUSATIVO)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .build();

        // Definir verbo transitivo en presente como OPCIONAL:
        // - criterio: busca verbos transitivos en presente entre las tarjetas SRS
        // - generador: si no hay verbo disponible, genera uno aleatorio de la BD
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(CriterioBusqueda.de(VerboFlexion.class)
                        .con(CaracteristicaGramatical.FORMA_VERBAL, FormaVerbal.PRESENT)
                        .con(CaracteristicaGramatical.TRANSITIVIDAD, Transitividad.TRANSITIVO)
                        .con(CaracteristicaGramatical.NEGATIVO, false)
                        .build())
                .generador(() -> verbosService.getVerboTransitivoPresenteAleatorio())
                .extractor(ExtractorVerbo.get())
                .build();

        // Definir apoyo de pronombre (depende del verbo)
        ElementoFrase<PronombreFlexion> pronombre = ElementoFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .generador(verbo, palabra -> pronombreService.getPronombre((VerboFlexion) palabra))
                .extractor(ExtractorPronombre.get())
                .build();

        // Definir apoyo de número (depende del CD)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(cd, palabra -> numeralService.getNumeral((SustantivoFlexion) palabra))
                .extractor(ExtractorNumero.get())
                .build();

        // Agregar en orden de visualización
        agregarElemento(pronombre);
        agregarElemento(verbo);
        agregarElemento(numero);
        agregarElemento(cd);
    }
}

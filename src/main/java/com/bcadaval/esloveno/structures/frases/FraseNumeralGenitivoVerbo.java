package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.ElementoFrase;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtractorSustantivo;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Estructura de frase: Número (≥5) + Sustantivo (GEN PL) + Verbo intransitivo (3ª SG)
 * <p>
 * Cubre la regla gramatical eslovena para numerales mayores o iguales a 5:
 * el numeral va en caso nominativo, el sustantivo pasa a <b>genitivo plural</b>
 * y el verbo va en <b>tercera persona singular</b> (el sujeto gramatical es el numeral).
 * <p>
 * Ejemplo: "pet knjig leži" (cinco libros yacen)
 * <p>
 * Esta es una regla fundamental del esloveno que difiere del comportamiento con
 * los numerales 1–4 (donde el sustantivo va en nominativo y el verbo concuerda en número).
 * La construcción es similar a las lenguas eslavas: con números altos, el sustantivo
 * adopta genitivo plural y el verbo vuelve a singular porque el sujeto sintáctico
 * es la expresión numérica completa.
 * <p>
 * Elementos (en orden de visualización):
 * <ol>
 *   <li><b>NUMERO</b> (apoyo): numeral ≥5 generado a partir del sustantivo.
 *       El NumeralService filtra numerales con principal distinto de "en" y "dva"
 *       cuando el número gramatical es PLURAL.</li>
 *   <li><b>SUSTANTIVO</b> (slot): SustantivoFlexion en caso GENITIVO, número PLURAL</li>
 *   <li><b>VERBO</b> (apoyo): verbo intransitivo/ambitransitivo en 3ª persona SINGULAR,
 *       siempre singular independientemente del numeral (regla gramatical de 5+)</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.INTERMEDIO)
public class FraseNumeralGenitivoVerbo extends EstructuraFrase {

    @Getter
    private final String identificador = "NUMERAL_GENITIVO_VERBO";
    @Getter
    private final String nombreMostrar = "Número (5+) + Sust. (GEN PL) + Verbo (intr, SG)";

    public FraseNumeralGenitivoVerbo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Slot de sustantivo en genitivo plural (obligatorio para SRS)
        // Filtra por caso GENITIVO y número PLURAL: la forma correcta para numerales ≥5
        ElementoFrase<SustantivoFlexion> sustantivo = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.GENITIVO)
                        .con(CaracteristicaGramatical.NUMERO, Numero.PLURAL)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .build();

        // Apoyo de numeral: generado a partir del sustantivo.
        // El NumeralService.getNumeral(SustantivoFlexion) usa numero=PLURAL, caso=GENITIVO y género del sustantivo.
        // Sin embargo, el numeral para 5+ debe ir en NOMINATIVO, no en genitivo.
        // Por eso usamos un generador manual que pide caso NOMINATIVO con número PLURAL.
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(sustantivo, palabra -> {
                    SustantivoFlexion sf = (SustantivoFlexion) palabra;
                    Genero genero = sf.getSustantivoBase() != null
                            ? sf.getSustantivoBase().getGenero() : null;
                    // Numeral en NOMINATIVO PLURAL (≥5): principal != "en" y != "dva"
                    return numeralService.getNumeral(Numero.PLURAL, Caso.NOMINATIVO, genero);
                })
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Apoyo de verbo: intransitivo/ambitransitivo en 3ª persona SINGULAR.
        // Regla de 5+: el verbo siempre va en singular, independientemente del número del sustantivo.
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .generador(sustantivo, palabra ->
                        verbosService.getVerboIntransitivoPresenteAleatorio(
                                Persona.TERCERA, Numero.SINGULAR))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Agregar en orden de visualización: NUMERO + SUSTANTIVO + VERBO
        agregarElemento(numero);
        agregarElemento(sustantivo);
        agregarElemento(verbo);
    }
}


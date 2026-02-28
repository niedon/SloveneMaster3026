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
 * Estructura de frase: Número + Sustantivo (NOM) + Verbo intransitivo (3ª persona)
 * <p>
 * Cubre los numerales 1–4 en esloveno, donde el sustantivo va en nominativo
 * y el verbo concuerda en número con el sustantivo:
 * <ul>
 *   <li><b>1 (en/ena/eno):</b> sustantivo NOM.SG + verbo 3ª.SG → "ena knjiga leži" (un libro yace)</li>
 *   <li><b>2 (dva/dve):</b> sustantivo NOM.DUAL + verbo 3ª.DUAL → "dve knjigi ležita" (dos libros yacen)</li>
 *   <li><b>3–4 (tri, štiri):</b> sustantivo NOM.PL + verbo 3ª.PL → "tri knjige ležijo" (tres libros yacen)</li>
 * </ul>
 * <p>
 * Regla gramatical eslovena: para los numerales 1 a 4, el sustantivo va en caso
 * nominativo con el número gramatical correspondiente (singular, dual, plural),
 * y el verbo concuerda en persona (3ª) y número con el sustantivo.
 * <p>
 * Elementos (en orden de visualización):
 * <ol>
 *   <li><b>NUMERO</b> (apoyo): numeral generado a partir del sustantivo (caso y número concordantes)</li>
 *   <li><b>SUSTANTIVO</b> (slot): SustantivoFlexion en caso NOMINATIVO (el número varía según la tarjeta SRS)</li>
 *   <li><b>VERBO</b> (apoyo): verbo intransitivo/ambitransitivo en 3ª persona, generado concordando
 *       en número con el sustantivo</li>
 * </ol>
 * <p>
 * El verbo es apoyo (no participa en SRS) porque debe concordar en número con el sustantivo
 * que se extraiga del SRS. El sustantivo es el elemento principal de aprendizaje.
 */
@Component
@DificultadFrase(NivelDificultad.INTERMEDIO)
public class FraseNumeralVerboIntransitivo extends EstructuraFrase {

    @Getter
    private final String identificador = "NUMERAL_VERBO_INTRANSITIVO";
    @Getter
    private final String nombreMostrar = "Número (1–4) + Sust. (NOM) + Verbo (intr)";

    public FraseNumeralVerboIntransitivo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Slot de sustantivo en nominativo (el número vendrá del SRS: singular, dual o plural)
        ElementoFrase<SustantivoFlexion> sustantivo = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.NOMINATIVO)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .build();

        // Apoyo de numeral: generado a partir del sustantivo (concordancia en caso, número y género)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(sustantivo, palabra -> numeralService.getNumeral((SustantivoFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Apoyo de verbo: intransitivo/ambitransitivo en 3ª persona,
        // concordando en número con el sustantivo
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .generador(sustantivo, palabra -> {
                    SustantivoFlexion sf = (SustantivoFlexion) palabra;
                    return verbosService.getVerboIntransitivoPresenteAleatorio(
                            Persona.TERCERA, sf.getNumero());
                })
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Agregar en orden de visualización: NUMERO + SUSTANTIVO + VERBO
        agregarElemento(numero);
        agregarElemento(sustantivo);
        agregarElemento(verbo);
    }
}



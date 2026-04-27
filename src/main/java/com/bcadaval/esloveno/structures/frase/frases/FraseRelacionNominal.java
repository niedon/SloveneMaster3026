package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.CategoriaFrase;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.ParticulaFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.services.palabra.sustantivo.SustantivoService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Estructura de frase que expresa una relación nominal del tipo <em>X de Y</em>,
 * equivalente en esloveno al uso del caso genitivo para expresar pertenencia o relación
 * entre dos sustantivos (p. ej. «la casa del vecino», «el libro de la biblioteca»).
 *
 * <h2>Estructura</h2>
 * <pre>
 *   [NUMERO_NUCLEO] SUSTANTIVO_NUCLEO [PARTICULA_DE] [NUMERO_DEPENDIENTE] SUSTANTIVO_DEPENDIENTE
 * </pre>
 * <ul>
 *   <li><strong>SUSTANTIVO_NUCLEO</strong>: núcleo de la frase.</li>
 *   <li><strong>NUMERO_NUCLEO</strong>: numeral que acompaña al núcleo, derivado automáticamente de él.</li>
 *   <li><strong>PARTICULA_DE</strong>: partícula fija «de» que actúa como nexo entre los dos sustantivos.</li>
 *   <li><strong>SUSTANTIVO_DEPENDIENTE</strong>: modificador del núcleo, en caso
 *       {@link com.bcadaval.esloveno.beans.enums.Caso#GENITIVO GENITIVO}.</li>
 *   <li><strong>NUMERO_DEPENDIENTE</strong>: numeral que acompaña al sustantivo dependiente,
 *       derivado automáticamente de él.</li>
 * </ul>
 *
 * <h2>Nivel de dificultad</h2>
 * {@link com.bcadaval.esloveno.beans.enums.NivelDificultad#INTERMEDIO INTERMEDIO}
 */
@Component
@DificultadFrase(categoria = CategoriaFrase.RELACION_NOMINAL)
public class FraseRelacionNominal extends Frase {

    @Autowired
    private SustantivoService sustantivoService;

    /**
     * Devuelve el identificador único de esta estructura de frase.
     *
     * @return {@code "RELACION_NOMINAL"}
     */
    @Override
    public String getIdentificador() {
        return "RELACION_NOMINAL";
    }

    /**
     * Devuelve el nombre legible de esta estructura para mostrar en la interfaz de usuario.
     *
     * @return {@code "Relación nominal (X de Y)"}
     */
    @Override
    public String getNombreMostrar() {
        return "Relación nominal (X de Y)";
    }

    /**
     * Inicializa los huecos ({@link com.bcadaval.esloveno.structures.frase.PalabraFrase PalabraFrase})
     * que componen esta estructura de frase y los registra en el orden de aparición.
     *
     * <p>Los huecos creados son, por orden de registro:
     * <ol>
     *   <li><strong>NUMERO_DEPENDIENTE</strong>: numeral generado a partir de {@code SUSTANTIVO_DEPENDIENTE}.</li>
     *   <li><strong>SUSTANTIVO_NUCLEO</strong>: sustantivo principal en nominativo.</li>
     *   <li><strong>PARTICULA_DE</strong>: partícula fija «de» sin búsqueda en la base de datos.</li>
     *   <li><strong>NUMERO_NUCLEO</strong>: numeral generado a partir de {@code SUSTANTIVO_NUCLEO}.</li>
     *   <li><strong>SUSTANTIVO_DEPENDIENTE</strong>: sustantivo modificador en genitivo.</li>
     * </ol>
     *
     * <p>Anotado con {@link jakarta.annotation.PostConstruct @PostConstruct} para que Spring
     * lo invoque automáticamente tras la inyección de dependencias.
     */
    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<SustantivoFlexion> sustantivoDependiente =  palabraFraseFactory.crearSustantivoAncla("SUSTANTIVO_DEPENDIENTE", Caso.GENITIVO);

        PalabraFrase<NumeralFlexion> numeralDependiente = palabraFraseFactory.crearNumeralApoyo("NUMERO_DEPENDIENTE", sustantivoDependiente);

        PalabraFrase<ParticulaFlexion> particulaDe = PalabraFrase.<ParticulaFlexion>builder()
                .nombre("PARTICULA_DE")
                .generador(() -> ParticulaFlexion.builder().flexion("de").build())
                .extractorDeEsloveno(x -> "")
                .extractorAEspanol(x -> "de")
                .extractorDeEspanol(x -> "de")
                .extractorAEsloveno(x -> "")
                .build();

        PalabraFrase<SustantivoFlexion> sustantivoNucleo =palabraFraseFactory.crearSustantivoOpcional("SUSTANTIVO_NUCLEO", Caso.NOMINATIVO);

        PalabraFrase<NumeralFlexion> numeralNucleo = palabraFraseFactory.crearNumeralApoyo("NUMERO_NUCLEO", sustantivoNucleo);

        agregarElemento(numeralNucleo);
        agregarElemento(sustantivoNucleo);
        agregarElemento(particulaDe);
        agregarElemento(numeralDependiente);
        agregarElemento(sustantivoDependiente);
    }
}

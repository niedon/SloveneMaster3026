package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.Numeral;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorNumero;
import com.bcadaval.esloveno.structures.extractores.ExtractorSustantivo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;
import com.bcadaval.esloveno.structures.frase.criterio.NumeralCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.SustantivoCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.dependencia.DependenciaBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Frase: Solo un Sustantivo en Nominativo.
 * <p>
 * Ejemplo: "El libro" → "Knjiga"
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>NUMERO</strong> (apoyo): numeral que concuerda con el sustantivo, generado a partir de este</li>
 *   <li><strong>SUSTANTIVO</strong> (obligatorio): {@link SustantivoFlexion} con caso NOMINATIVO</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.PRINCIPIANTE)
public class FraseSoloSustantivoNominativoNueva extends Frase {

    @Autowired
    private NumeralService numeralService;

    @Override
    public String getIdentificador() {
        return "SOLO_SUSTANTIVO_NOMINATIVO";
    }

    @Override
    public String getNombreMostrar() {
        return "Sustantivo (NOM)";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<SustantivoFlexion> sustantivo = PalabraFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .criterio(SustantivoCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .build();

        PalabraFrase<NumeralFlexion> numeral = PalabraFrase.<NumeralFlexion>builder()
                .nombre("NUMERO2")
                .criterio(NumeralCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .conDependencia(DependenciaBuilder.de(sustantivo)
                                .si(sus -> sus.getNumero() == Numero.SINGULAR, NumeralCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(sus -> sus.getNumero() == Numero.DUAL, NumeralCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(NumeralCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        .build()
                )
                .extractor(ExtractorNumero.get())
                .build();

        agregarElemento(numeral);
        agregarElemento(sustantivo);
    }
}

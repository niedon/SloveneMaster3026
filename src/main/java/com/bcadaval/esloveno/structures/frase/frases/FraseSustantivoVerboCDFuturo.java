package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
@DificultadFrase(NivelDificultad.AVANZADO)
public class FraseSustantivoVerboCDFuturo extends Frase {

    @Override
    public String getIdentificador() {
        return "SUSTANTIVO_VERBO_CD_FUTURO";
    }

    @Override
    public String getNombreMostrar() {
        return "Futuro + CD (Sujeto Nominal)";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<VerboFlexion> participio = palabraFraseFactory.crearVerboParticipioAncla("VERBO");
        PalabraFrase<SustantivoFlexion> sujeto = palabraFraseFactory.crearSustantivoDependienteParticipio("SUJETO", participio);
        PalabraFrase<VerboFlexion> auxiliar = palabraFraseFactory.crearBitiAuxiliarFuturoParaSustantivo("VERBO_AUXILIAR", sujeto);
        PalabraFrase<NumeralFlexion> numeralSujeto = palabraFraseFactory.crearNumeralOpcional("NUMERAL_SUJETO", sujeto, Caso.NOMINATIVO);
        PalabraFrase<SustantivoFlexion> sustantivoCD = palabraFraseFactory.crearSustantivoOpcional("CD", Caso.ACUSATIVO);
        PalabraFrase<NumeralFlexion> numeralCD = palabraFraseFactory.crearNumeralOpcional("NUMERAL_CD", sustantivoCD, Caso.ACUSATIVO);

        agregarElemento(numeralSujeto);
        agregarElemento(sujeto);
        agregarElemento(auxiliar);
        agregarElemento(participio);
        agregarElemento(numeralCD);
        agregarElemento(sustantivoCD);
    }
}


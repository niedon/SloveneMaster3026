package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
@DificultadFrase(NivelDificultad.AVANZADO)
public class FraseVerboCDFuturo extends Frase {

    @Override
    public String getIdentificador() {
        return "VERBO_CD_FUTURO";
    }

    @Override
    public String getNombreMostrar() {
        return "Futuro + CD (Sujeto Pronombre)";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<VerboFlexion> participio = palabraFraseFactory.crearVerboParticipioAncla("VERBO", Transitividad.TRANSITIVO, Transitividad.AMBITRANSITIVO);
        PalabraFrase<PronombreFlexion> pronombre = palabraFraseFactory.crearPronombreParaVerboParticipio("PRONOMBRE", participio);
        PalabraFrase<VerboFlexion> auxiliar = palabraFraseFactory.crearBitiAuxiliarFuturoParaPronombre("VERBO_AUXILIAR", pronombre);
        PalabraFrase<SustantivoFlexion> sustantivoCD = palabraFraseFactory.crearSustantivoOpcional("SUSTANTIVO_CD", Caso.ACUSATIVO);
        PalabraFrase<NumeralFlexion> numeralCD = palabraFraseFactory.crearNumeralOpcional("NUMERAL_CD", sustantivoCD, Caso.ACUSATIVO);

        agregarElemento(pronombre);
        agregarElemento(auxiliar);
        agregarElemento(participio);
        agregarElemento(numeralCD);
        agregarElemento(sustantivoCD);
    }
}


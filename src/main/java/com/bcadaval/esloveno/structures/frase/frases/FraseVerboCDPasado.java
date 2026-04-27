package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.CategoriaFrase;
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
@DificultadFrase(categoria = CategoriaFrase.PASADO_CON_CD)
public class FraseVerboCDPasado extends Frase {

    @Override
    public String getIdentificador() {
        return "VERBO_CD_PASADO";
    }

    @Override
    public String getNombreMostrar() {
        return "Pasado + CD (Sujeto Pronombre)";
    }

    @PostConstruct
    public void configurarEstructura() {
        // 1. PARTICIPIO: Transitivo
        PalabraFrase<VerboFlexion> participio = palabraFraseFactory.crearVerboParticipioAncla("VERBO", Transitividad.TRANSITIVO, Transitividad.AMBITRANSITIVO);

        // 2. PRONOMBRE: Dependencia doble (Num, Gen)
        PalabraFrase<PronombreFlexion> pronombre = palabraFraseFactory.crearPronombreParaVerboParticipio("PRONOMBRE", participio);

        // 3. AUXILIAR
        PalabraFrase<VerboFlexion> auxiliar = palabraFraseFactory.crearBitiAuxiliarPasadoParaPronombre("VERBO_AUXILIAR", pronombre);

        // 4. CD: Sustantivo en Acusativo
        PalabraFrase<SustantivoFlexion> cd = palabraFraseFactory.crearSustantivoOpcional("CD", Caso.ACUSATIVO);

        PalabraFrase<NumeralFlexion> numeralCD = palabraFraseFactory.crearNumeralApoyo("NUMERO_CD", cd);

        agregarElemento(pronombre);
        agregarElemento(auxiliar);
        agregarElemento(participio);
        agregarElemento(numeralCD);
        agregarElemento(cd);
    }
}

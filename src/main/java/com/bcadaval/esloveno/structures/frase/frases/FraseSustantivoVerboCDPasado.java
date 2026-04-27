package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.CategoriaFrase;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
@DificultadFrase(categoria = CategoriaFrase.PASADO_CON_CD)
public class FraseSustantivoVerboCDPasado extends Frase {

    @Override
    public String getIdentificador() {
        return "SUSTANTIVO_VERBO_CD_PASADO";
    }

    @Override
    public String getNombreMostrar() {
        return "Pasado + CD (Sujeto Sustantivo)";
    }

    @PostConstruct
    public void configurarEstructura() {
        // 1. PARTICIPIO (Ancla): Transitivo
        PalabraFrase<VerboFlexion> participio = palabraFraseFactory.crearVerboParticipioAncla("VERBO", Transitividad.TRANSITIVO, Transitividad.AMBITRANSITIVO);

        // 2. SUJETO: Depende de Participio
        PalabraFrase<SustantivoFlexion> sujeto = palabraFraseFactory.crearSustantivoDependienteParticipio("SUJETO", participio);

        // 3. NUMERAL
        PalabraFrase<NumeralFlexion> numeralSujeto = palabraFraseFactory.crearNumeralApoyo("NUMERO_SUJETO", sujeto);

        // 4. CD: Depende de Participio (solo existencia)
        PalabraFrase<SustantivoFlexion> cd = palabraFraseFactory.crearSustantivoOpcional("CD", Caso.ACUSATIVO);

        PalabraFrase<NumeralFlexion> numeralCD = palabraFraseFactory.crearNumeralApoyo("NUMERO_CD", cd);

        // 5. AUXILIAR (3ª Persona)
        PalabraFrase<VerboFlexion> auxiliar = palabraFraseFactory.crearBitiAuxiliarPasadoParaSustantivo("VERBO_AUXILIAR", sujeto);

        agregarElemento(numeralSujeto);
        agregarElemento(sujeto);
        agregarElemento(auxiliar);
        agregarElemento(participio);
        agregarElemento(numeralCD);
        agregarElemento(cd);
    }
}

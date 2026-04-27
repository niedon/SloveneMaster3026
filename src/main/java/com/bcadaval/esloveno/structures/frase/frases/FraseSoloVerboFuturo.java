package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.CategoriaFrase;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
@DificultadFrase(categoria = CategoriaFrase.FUTURO_SIMPLE)
public class FraseSoloVerboFuturo extends Frase {

    @Override
    public String getIdentificador() {
        return "SOLO_VERBO_FUTURO";
    }

    @Override
    public String getNombreMostrar() {
        return "Futuro (Sujeto Pronombre)";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<VerboFlexion> participio = palabraFraseFactory.crearVerboParticipioAncla("VERBO");
        PalabraFrase<PronombreFlexion> pronombre = palabraFraseFactory.crearPronombreParaVerboParticipio("PRONOMBRE", participio);
        PalabraFrase<VerboFlexion> auxiliar = palabraFraseFactory.crearBitiAuxiliarFuturoParaPronombre("VERBO_AUXILIAR", pronombre);

        agregarElemento(pronombre);
        agregarElemento(auxiliar);
        agregarElemento(participio);
    }
}


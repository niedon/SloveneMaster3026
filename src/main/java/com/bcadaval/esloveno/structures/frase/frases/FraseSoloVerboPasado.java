package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.CategoriaFrase;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
@DificultadFrase(categoria = CategoriaFrase.PASADO_SIMPLE)
public class FraseSoloVerboPasado extends Frase {

    @Override
    public String getIdentificador() {
        return "SOLO_VERBO_PASADO";
    }

    @Override
    public String getNombreMostrar() {
        return "Pasado (Sujeto Pronombre)";
    }

    @PostConstruct
    public void configurarEstructura() {
        // 1. PARTICIPIO (Ancla): Define Género y Número aleatorios
        PalabraFrase<VerboFlexion> participio = palabraFraseFactory.crearVerboParticipioAncla("VERBO");

        // 2. PRONOMBRE (Depende del participio)
        PalabraFrase<PronombreFlexion> pronombre = palabraFraseFactory.crearPronombreParaVerboParticipio("PRONOMBRE", participio);

        // 3. AUXILIAR (Depende del pronombre)
        PalabraFrase<VerboFlexion> auxiliar = palabraFraseFactory.crearBitiAuxiliarPasadoParaPronombre("VERBO_AUXILIAR", pronombre);

        agregarElemento(pronombre);
        agregarElemento(auxiliar);
        agregarElemento(participio);
    }
}

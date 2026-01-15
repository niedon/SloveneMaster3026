package com.bcadaval.esloveno.structures.frases;

import java.util.Set;

import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.ElementoFrase;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import com.bcadaval.esloveno.structures.specifications.SustantivoFlexionSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.structures.EstructuraFrase;

import jakarta.annotation.PostConstruct;

/**
 * Estructura de frase: Solo un Sustantivo Nominativo
 * <p>
 * Ejemplo: "El libro" → "Knjiga"
 * <p>
 * Elementos:
 * 1. NUMERO (apoyo): numeral que concuerda con el sustantivo
 * 2. SUSTANTIVO (slot): SustantivoFlexion con caso NOMINATIVO
 */
@Component
public class FraseSoloSustantivoNominativo extends EstructuraFrase {

    public static final String IDENTIFICADOR = "SOLO_SUSTANTIVO_NOMINATIVO";
    public static final String NOMBRE_MOSTRAR = "Sustantivo (NOM)";

    @Autowired
    private NumeralService numeralService;

    @Autowired
    private ExtraccionSlotEstandar extraccionSlotEstandar;

    @Autowired
    private ExtraccionApoyoEstandar extraccionApoyoEstandar;


    public FraseSoloSustantivoNominativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de sustantivo
        ElementoFrase<SustantivoFlexion> sustantivo = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .criterio(CriterioBusqueda.de(
                        SustantivoFlexion.class,
                        sf -> sf.getCaso() == Caso.NOMINATIVO && sf.getSustantivoBase() != null,
                        SustantivoFlexionSpecs.conCasoYBase(Caso.NOMINATIVO)
                ))
                .extractor(extraccionSlotEstandar)
                .build();

        // Definir apoyo de número (depende del sustantivo)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(sustantivo, palabra -> {
                    SustantivoFlexion sf = (SustantivoFlexion) palabra;
                    return numeralService.getNumeral(sf);
                })
                .extractor(extraccionApoyoEstandar)
                .build();

        // Agregar en orden de visualización
        agregarElemento(numero);
        agregarElemento(sustantivo);
    }

    @Override
    public String getIdentificador() {
        return IDENTIFICADOR;
    }

    @Override
    public String getNombreMostrar() {
        return NOMBRE_MOSTRAR;
    }

    @Override
    public Set<Caso> getCasosUsados() {
        return Set.of(Caso.NOMINATIVO);
    }

    @Override
    public Set<FormaVerbal> getFormasVerbalesUsadas() {
        return Set.of();
    }
}


package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.ElementoFrase;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.ExcluirDeFrases;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Estructura de frase: Verbo en pasado (participio) + Objeto directo en Acusativo
 * <p>
 * Ejemplo: "Sem prebral knjigo" (He leído el libro), "Sem videl film" (He visto la película)
 * <p>
 * Esta estructura practica el pasado con objeto directo, esencial para narrar:
 * - Experiencias: Sem obiskal muzej (He visitado el museo)
 * - Acciones completadas: Sem napisal pismo (He escrito una carta)
 * - Conversación cotidiana: Sem kupil kruh (He comprado pan)
 * <p>
 * El participio concuerda en género y número con el sujeto.
 * <p>
 * Elementos:
 * 1. PRONOMBRE (apoyo): pronombre personal del sujeto
 * 2. VERBO (slot): VerboFlexion en forma PARTICIPLE
 * 3. NUMERO (apoyo): numeral que concuerda con el objeto
 * 4. OBJETO (slot): SustantivoFlexion en ACUSATIVO
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FrasePasadoConObjeto extends EstructuraFrase {

    @Getter
    private final String identificador = "PASADO_CON_OBJETO";
    @Getter
    private final String nombreMostrar = "Pasado + Objeto (ACU)";

    public FrasePasadoConObjeto() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de verbo en participio
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(CriterioBusqueda.de(VerboFlexion.class)
                        .con(CaracteristicaGramatical.FORMA_VERBAL, FormaVerbal.PARTICIPLE)
                        .con(CaracteristicaGramatical.NEGATIVO, false)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .extractorDeEspanol(p ->
                        String.format("(%s) %s", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado()))
                .extractorAEspanol(p ->
                        String.format("(%s) %s", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado()))
                .build();

        // Definir slot de sustantivo en acusativo (objeto directo)
        ElementoFrase<SustantivoFlexion> objeto = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("OBJETO")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.ACUSATIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        // Definir apoyo de pronombre (depende del verbo)
        ElementoFrase<PronombreFlexion> pronombre = ElementoFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .generador(verbo, palabra -> pronombreService.getPronombre((VerboFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Definir apoyo de número (depende del objeto)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(objeto, palabra -> numeralService.getNumeral((SustantivoFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Agregar en orden de visualización
        agregarElemento(pronombre);
        agregarElemento(verbo);
        agregarElemento(numero);
        agregarElemento(objeto);
    }
}

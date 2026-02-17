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
 * Estructura de frase: Verbo en imperativo + Objeto directo en Acusativo
 * <p>
 * Ejemplo: "Preberi knjigo!" (¡Lee el libro!), "Poglej sliko!" (¡Mira la foto!)
 * <p>
 * Esta estructura combina el imperativo con un objeto directo, muy útil para:
 * - Instrucciones: Odpri okno! (¡Abre la ventana!)
 * - Peticiones: Daj mi vodo! (¡Dame agua!)
 * - Recetas y manuales: Dodaj sol! (¡Añade sal!)
 * <p>
 * Fundamental para nivel A1-A2 para dar y entender instrucciones.
 * <p>
 * Elementos:
 * 1. VERBO (slot): VerboFlexion en forma IMPERATIVE
 * 2. NUMERO (apoyo): numeral que concuerda con el objeto
 * 3. OBJETO (slot): SustantivoFlexion en ACUSATIVO (objeto directo)
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseImperativoConObjeto extends EstructuraFrase {

    @Getter
    private final String identificador = "IMPERATIVO_CON_OBJETO";
    @Getter
    private final String nombreMostrar = "Imperativo + Objeto (ACU)";

    public FraseImperativoConObjeto() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de verbo en imperativo
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(CriterioBusqueda.de(VerboFlexion.class)
                        .con(CaracteristicaGramatical.FORMA_VERBAL, FormaVerbal.IMPERATIVE)
                        .con(CaracteristicaGramatical.NEGATIVO, false)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .extractorDeEspanol(p ->
                        String.format("(%s) ¡%s!", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado()))
                .extractorAEspanol(p ->
                        String.format("(%s) ¡%s!", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado()))
                .build();

        // Definir slot de sustantivo en acusativo (objeto directo)
        ElementoFrase<SustantivoFlexion> objeto = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("OBJETO")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.ACUSATIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        // Definir apoyo de número (depende del objeto)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(objeto, palabra -> numeralService.getNumeral((SustantivoFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Agregar en orden de visualización
        agregarElemento(verbo);
        agregarElemento(numero);
        agregarElemento(objeto);
    }
}

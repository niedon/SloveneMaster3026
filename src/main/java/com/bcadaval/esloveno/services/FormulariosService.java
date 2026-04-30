package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.rest.dto.FieldSchemaDTO;
import com.bcadaval.esloveno.rest.dto.OptionDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FormulariosService {

    public Map<String, List<FieldSchemaDTO>> generarEsquemaFormulario() {
        Map<String, List<FieldSchemaDTO>> esquema = new HashMap<>();

        FieldSchemaDTO significado = FieldSchemaDTO.builder()
                .name("significado")
                .label("Significado en español")
                .inputType("TEXT")
                .build();
        FieldSchemaDTO animacidad = FieldSchemaDTO.builder()
                .name("animacidad")
                .label("Animacidad")
                .inputType("SELECT")
                .options(obtenerOpciones(Animacidad.class))
                .build();
        FieldSchemaDTO contabilidad = FieldSchemaDTO.builder()
                .name("contabilidad")
                .label("Contabilidad")
                .inputType("SELECT")
                .options(obtenerOpciones(Contabilidad.class))
                .build();
        FieldSchemaDTO claseSemantica = FieldSchemaDTO.builder()
                .name("claseSemantica")
                .label("Clase semántica")
                .inputType("SELECT")
                .options(obtenerOpciones(ClaseSemantica.class))
                .build();
        FieldSchemaDTO transitividad = FieldSchemaDTO.builder()
                .name("transitividad")
                .label("Transitividad")
                .inputType("SELECT")
                .options(obtenerOpciones(Transitividad.class))
                .build();
        FieldSchemaDTO requiereSujetoAnimado = FieldSchemaDTO.builder()
                .name("requiereSujetoAnimado")
                .label("¿Requiere sujeto animado?")
                .inputType("SELECT")
                .options(obtenerOpciones(RequiereSujetoAnimado.class))
                .build();
        FieldSchemaDTO requiereObjetoAnimado = FieldSchemaDTO.builder()
                .name("requiereObjetoAnimado")
                .label("¿Requiere objeto animado?")
                .inputType("SELECT")
                .options(obtenerOpciones(RequiereObjetoAnimado.class))
                .build();
        FieldSchemaDTO cantidad = FieldSchemaDTO.builder()
                .name("cantidad")
                .label("Cantidad (valor numérico)")
                .inputType("NUMBER")
                .placeholder("Ej: 1 para en, 2 para dva, 5 para pet...")
                .build();

        // SUSTANTIVO
        esquema.put(TipoPalabra.SUSTANTIVO.name(), List.of(
                significado,
                animacidad,
                contabilidad,
                claseSemantica
        ));

        // VERBO
        esquema.put(TipoPalabra.VERBO.name(), List.of(
                significado,
                transitividad,
                requiereSujetoAnimado,
                requiereObjetoAnimado
        ));

        // ADJETIVO
        esquema.put(TipoPalabra.ADJETIVO.name(), List.of(significado));

        // PRONOMBRE
        esquema.put(TipoPalabra.PRONOMBRE.name(), List.of(significado));

        // PARTICULA
        esquema.put(TipoPalabra.PARTICULA.name(), List.of(significado));

        // NUMERAL
        esquema.put(TipoPalabra.NUMERAL.name(), List.of(cantidad));

        return esquema;
    }

    private <E extends Enum<E>> List<OptionDTO> obtenerOpciones(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> OptionDTO.builder().value(e.name()).label(StringUtils.capitalize(e.name().toLowerCase())).build())
                .toList();
    }
}


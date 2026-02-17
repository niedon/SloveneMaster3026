package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.Sustantivo;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests para ElementoFrase - elemento de estructura de frase con lógica de asignación y extracción.
 */
@DisplayName("ElementoFrase")
class ElementoFraseTest {

    private SustantivoFlexion sustantivoNominativo;
    private SustantivoFlexion sustantivoAcusativo;
    private Sustantivo sustantivoBase;

    @BeforeEach
    void setUp() {
        sustantivoBase = Sustantivo.builder()
                .significado("perro")
                .sloleksId("test-123")
                .build();

        sustantivoNominativo = SustantivoFlexion.builder()
                .id(1)
                .caso(Caso.NOMINATIVO)
                .numero(Numero.SINGULAR)
                .flexion("pes")
                .acentuado("pés")
                .sustantivoBase(sustantivoBase)
                .build();

        sustantivoAcusativo = SustantivoFlexion.builder()
                .id(2)
                .caso(Caso.ACUSATIVO)
                .numero(Numero.SINGULAR)
                .flexion("psa")
                .acentuado("psá")
                .sustantivoBase(sustantivoBase)
                .build();
    }

    @Nested
    @DisplayName("Builder validaciones")
    class BuilderValidaciones {

        @Test
        @DisplayName("lanza excepción si no tiene nombre")
        void lanzaExcepcionSinNombre() {
            assertThatThrownBy(() ->
                    ElementoFrase.<SustantivoFlexion>builder()
                            .criterio(CriterioBusqueda.de(SustantivoFlexion.class).build())
                            .extractor(ExtraccionSlotEstandar.get())
                            .build()
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("nombre");
        }

        @Test
        @DisplayName("lanza excepción si no tiene criterio ni generador")
        void lanzaExcepcionSinCriterioNiGenerador() {
            assertThatThrownBy(() ->
                    ElementoFrase.<SustantivoFlexion>builder()
                            .nombre("TEST")
                            .extractor(ExtraccionSlotEstandar.get())
                            .build()
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("criterio")
                    .hasMessageContaining("generador");
        }

        @Test
        @DisplayName("lanza excepción si no tiene extractor ni estrategia")
        void lanzaExcepcionSinExtractor() {
            assertThatThrownBy(() ->
                    ElementoFrase.<SustantivoFlexion>builder()
                            .nombre("TEST")
                            .criterio(CriterioBusqueda.de(SustantivoFlexion.class).build())
                            .build()
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("estrategia")
                    .hasMessageContaining("extractores");
        }

        @Test
        @DisplayName("construye correctamente un slot válido")
        void construyeSlotValido() {
            ElementoFrase<SustantivoFlexion> elemento = ElementoFrase.<SustantivoFlexion>builder()
                    .nombre("SUSTANTIVO")
                    .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                            .con(CaracteristicaGramatical.CASO, Caso.NOMINATIVO)
                            .build())
                    .extractor(ExtraccionSlotEstandar.get())
                    .build();

            assertThat(elemento.getNombre()).isEqualTo("SUSTANTIVO");
            assertThat(elemento.esSlot()).isTrue();
            assertThat(elemento.esApoyo()).isFalse();
        }
    }

    @Nested
    @DisplayName("esSlot() y esApoyo()")
    class TipoElemento {

        @Test
        @DisplayName("esSlot retorna true para elemento con criterio")
        void esSlotConCriterio() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();

            assertThat(slot.esSlot()).isTrue();
            assertThat(slot.esApoyo()).isFalse();
        }

        @Test
        @DisplayName("esApoyo retorna true para elemento con generador")
        void esApoyoConGenerador() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();

            ElementoFrase<SustantivoFlexion> apoyo = ElementoFrase.<SustantivoFlexion>builder()
                    .nombre("APOYO")
                    .generador(slot, palabra -> sustantivoAcusativo)
                    .extractor(ExtraccionSlotEstandar.get())
                    .build();

            assertThat(apoyo.esApoyo()).isTrue();
            assertThat(apoyo.esSlot()).isFalse();
        }
    }

    @Nested
    @DisplayName("coincide()")
    class Coincide {

        @Test
        @DisplayName("retorna true si slot vacío y palabra cumple criterio")
        void retornaTrueSiVacioYCumple() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();

            assertThat(slot.coincide(sustantivoNominativo)).isTrue();
        }

        @Test
        @DisplayName("retorna false si palabra no cumple criterio")
        void retornaFalseSiNoCumple() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();

            assertThat(slot.coincide(sustantivoAcusativo)).isFalse();
        }

        @Test
        @DisplayName("retorna false si slot ya tiene palabra asignada")
        void retornaFalseSiYaAsignado() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();
            slot.asignar(sustantivoNominativo);

            // Crear otra palabra nominativo diferente
            SustantivoFlexion otraNominativo = SustantivoFlexion.builder()
                    .caso(Caso.NOMINATIVO)
                    .build();

            assertThat(slot.coincide(otraNominativo)).isFalse();
        }

        @Test
        @DisplayName("retorna false para palabra null")
        void retornaFalseParaNull() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();

            assertThat(slot.coincide(null)).isFalse();
        }

        @Test
        @DisplayName("retorna false si no es un slot")
        void retornaFalseSiNoEsSlot() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();
            ElementoFrase<SustantivoFlexion> apoyo = ElementoFrase.<SustantivoFlexion>builder()
                    .nombre("APOYO")
                    .generador(slot, p -> sustantivoAcusativo)
                    .extractor(ExtraccionSlotEstandar.get())
                    .build();

            assertThat(apoyo.coincide(sustantivoNominativo)).isFalse();
        }
    }

    @Nested
    @DisplayName("asignar() y estaAsignado()")
    class Asignacion {

        @Test
        @DisplayName("estaAsignado retorna false inicialmente")
        void estaAsignadoFalseInicialmente() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();

            assertThat(slot.estaAsignado()).isFalse();
        }

        @Test
        @DisplayName("estaAsignado retorna true después de asignar")
        void estaAsignadoTrueDespuesDeAsignar() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();
            slot.asignar(sustantivoNominativo);

            assertThat(slot.estaAsignado()).isTrue();
            assertThat(slot.getPalabraAsignada()).isEqualTo(sustantivoNominativo);
        }

        @Test
        @DisplayName("limpiar resetea la asignación")
        void limpiarResetea() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();
            slot.asignar(sustantivoNominativo);
            slot.limpiar();

            assertThat(slot.estaAsignado()).isFalse();
            assertThat(slot.getPalabraAsignada()).isNull();
        }
    }

    @Nested
    @DisplayName("getTextoFila1() y getTextoFila2()")
    class TextoVisualizacion {

        @Test
        @DisplayName("retorna cadena vacía si no hay palabra asignada")
        void retornaCadenaVaciaSinAsignacion() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();

            assertThat(slot.getTextoFila1(ModoVisualizacion.ES_SL)).isEmpty();
            assertThat(slot.getTextoFila2(ModoVisualizacion.ES_SL)).isEmpty();
        }

        @Test
        @DisplayName("modo ES_SL: fila1=significado, fila2=acentuado")
        void modoEsSl() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();
            slot.asignar(sustantivoNominativo);

            assertThat(slot.getTextoFila1(ModoVisualizacion.ES_SL)).isEqualTo("perro");
            assertThat(slot.getTextoFila2(ModoVisualizacion.ES_SL)).isEqualTo("pés");
        }

        @Test
        @DisplayName("modo SL_ES: fila1=flexion, fila2=significado")
        void modoSlEs() {
            ElementoFrase<SustantivoFlexion> slot = crearSlotNominativo();
            slot.asignar(sustantivoNominativo);

            assertThat(slot.getTextoFila1(ModoVisualizacion.SL_ES)).isEqualTo("pes");
            assertThat(slot.getTextoFila2(ModoVisualizacion.SL_ES)).isEqualTo("perro");
        }

        @Test
        @DisplayName("extractor individual sobreescribe estrategia")
        void extractorIndividualSobreescribe() {
            ElementoFrase<SustantivoFlexion> slot = ElementoFrase.<SustantivoFlexion>builder()
                    .nombre("TEST")
                    .criterio(CriterioBusqueda.de(SustantivoFlexion.class).build())
                    .extractor(ExtraccionSlotEstandar.get())
                    .extractorDeEspanol(p -> "CUSTOM: " + p.getSignificado())
                    .build();

            slot.asignar(sustantivoNominativo);

            assertThat(slot.getTextoFila1(ModoVisualizacion.ES_SL)).isEqualTo("CUSTOM: perro");
        }
    }

    // Helper para crear un slot de sustantivo nominativo
    private ElementoFrase<SustantivoFlexion> crearSlotNominativo() {
        return ElementoFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.NOMINATIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();
    }
}

package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests para RepeticionEspaciadaService - algoritmo SM-2.
 * Verifica que el algoritmo de repetición espaciada funciona correctamente.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RepeticionEspaciadaService")
@SuppressWarnings("unused") // Mocks necesarios para @InjectMocks aunque no se usen directamente
class RepeticionEspaciadaServiceTest {

    @Mock
    private VariablesService variablesService;

    @Mock
    private SustantivoFlexionRepo sustantivoFlexionRepo;

    @Mock
    private VerboFlexionRepo verboFlexionRepo;

    @Mock
    private AdjetivoFlexionRepo adjetivoFlexionRepo;

    @Mock
    private EstructuraFraseService estructuraFraseService;

    @Mock
    private ConsultaPalabrasService consultaPalabrasService;

    @InjectMocks
    private RepeticionEspaciadaService service;

    @BeforeEach
    void setUp() {
        // Configuración por defecto de variables (valores típicos SM-2)
        lenient().when(variablesService.getFactorFacilidadInicial()).thenReturn(2.5);
        lenient().when(variablesService.getFactorFacilidadMinimo()).thenReturn(1.3);
        lenient().when(variablesService.getIntervaloInicialSegundos()).thenReturn(600L); // 10 min
        lenient().when(variablesService.getIntervaloSegundaSegundos()).thenReturn(3600L); // 1 hora
        lenient().when(variablesService.getIntervaloReaprendizajeSegundos()).thenReturn(30L); // 30 seg
        lenient().when(variablesService.getPenalizacionFallo()).thenReturn(0.2);
    }

    @Nested
    @DisplayName("procesarRespuesta() - Acierto (recordó)")
    class ProcesarRespuestaAcierto {

        @Test
        @DisplayName("primera respuesta correcta: intervalo inicial, incrementa veces correctas")
        void primeraRespuestaCorrecta() {
            SustantivoFlexion flexion = crearFlexionNueva();
            Instant antes = Instant.now();

            service.procesarRespuesta(flexion, true);

            assertThat(flexion.getVecesConsecutivasCorrectas()).isEqualTo(1);
            assertThat(flexion.getIntervaloRepeticionSegundos()).isEqualTo(600L);
            assertThat(flexion.getTotalRevisiones()).isEqualTo(1);
            assertThat(flexion.getTotalAciertos()).isEqualTo(1);
            assertThat(flexion.getEnReaprendizaje()).isFalse();
            assertThat(flexion.getUltimaRevision()).isAfterOrEqualTo(antes);
            assertThat(flexion.getProximaRevision()).isAfter(antes);
            verify(sustantivoFlexionRepo).save(flexion);
        }

        @Test
        @DisplayName("segunda respuesta correcta: intervalo segunda")
        void segundaRespuestaCorrecta() {
            SustantivoFlexion flexion = crearFlexionConUnaCorrecta();

            service.procesarRespuesta(flexion, true);

            assertThat(flexion.getVecesConsecutivasCorrectas()).isEqualTo(2);
            assertThat(flexion.getIntervaloRepeticionSegundos()).isEqualTo(3600L);
            assertThat(flexion.getTotalAciertos()).isEqualTo(2);
        }

        @Test
        @DisplayName("tercera respuesta o más: intervalo = anterior * factorFacilidad")
        void terceraRespuestaOMas() {
            SustantivoFlexion flexion = SustantivoFlexion.builder()
                    .vecesConsecutivasCorrectas(2)
                    .factorFacilidad(2.5)
                    .intervaloRepeticionSegundos(3600L)
                    .totalRevisiones(2)
                    .totalAciertos(2)
                    .build();

            service.procesarRespuesta(flexion, true);

            assertThat(flexion.getVecesConsecutivasCorrectas()).isEqualTo(3);
            // 3600 * 2.5 = 9000
            assertThat(flexion.getIntervaloRepeticionSegundos()).isEqualTo(9000L);
        }

        @Test
        @DisplayName("acierto quita flag de reaprendizaje")
        void aciertoQuitaReaprendizaje() {
            SustantivoFlexion flexion = crearFlexionNueva();
            flexion.setEnReaprendizaje(true);

            service.procesarRespuesta(flexion, true);

            assertThat(flexion.getEnReaprendizaje()).isFalse();
        }
    }

    @Nested
    @DisplayName("procesarRespuesta() - Fallo (no recordó)")
    class ProcesarRespuestaFallo {

        @Test
        @DisplayName("fallo: resetea veces correctas a 0")
        void falloReseteaVecesCorrectas() {
            SustantivoFlexion flexion = crearFlexionConUnaCorrecta();

            service.procesarRespuesta(flexion, false);

            assertThat(flexion.getVecesConsecutivasCorrectas()).isZero();
        }

        @Test
        @DisplayName("fallo: aplica intervalo de reaprendizaje")
        void falloAplicaIntervaloReaprendizaje() {
            SustantivoFlexion flexion = crearFlexionConUnaCorrecta();

            service.procesarRespuesta(flexion, false);

            assertThat(flexion.getIntervaloRepeticionSegundos()).isEqualTo(30L);
        }

        @Test
        @DisplayName("fallo: activa flag de reaprendizaje")
        void falloActivaReaprendizaje() {
            SustantivoFlexion flexion = crearFlexionNueva();

            service.procesarRespuesta(flexion, false);

            assertThat(flexion.getEnReaprendizaje()).isTrue();
        }

        @Test
        @DisplayName("fallo: reduce factor de facilidad respetando mínimo")
        void falloReduceFactorFacilidad() {
            SustantivoFlexion flexion = SustantivoFlexion.builder()
                    .factorFacilidad(2.5)
                    .build();

            service.procesarRespuesta(flexion, false);

            // 2.5 - 0.2 = 2.3
            assertThat(flexion.getFactorFacilidad()).isEqualTo(2.3);
        }

        @Test
        @DisplayName("fallo: factor de facilidad no baja del mínimo")
        void falloFactorFacilidadNoMenorQueMinimo() {
            SustantivoFlexion flexion = SustantivoFlexion.builder()
                    .factorFacilidad(1.4) // Cerca del mínimo
                    .build();

            service.procesarRespuesta(flexion, false);

            // 1.4 - 0.2 = 1.2, pero mínimo es 1.3
            assertThat(flexion.getFactorFacilidad()).isEqualTo(1.3);
        }

        @Test
        @DisplayName("fallo: incrementa total de revisiones pero no aciertos")
        void falloIncrementaRevisionesPeroNoAciertos() {
            SustantivoFlexion flexion = SustantivoFlexion.builder()
                    .totalRevisiones(5)
                    .totalAciertos(4)
                    .build();

            service.procesarRespuesta(flexion, false);

            assertThat(flexion.getTotalRevisiones()).isEqualTo(6);
            assertThat(flexion.getTotalAciertos()).isEqualTo(4); // No cambia
        }
    }

    @Nested
    @DisplayName("procesarRespuesta() - Casos límite")
    class CasosLimite {

        @Test
        @DisplayName("maneja flexión con todos los campos null")
        void manejaFlexionConCamposNull() {
            SustantivoFlexion flexion = SustantivoFlexion.builder().build();

            service.procesarRespuesta(flexion, true);

            assertThat(flexion.getVecesConsecutivasCorrectas()).isEqualTo(1);
            assertThat(flexion.getFactorFacilidad()).isEqualTo(2.5);
            assertThat(flexion.getTotalRevisiones()).isEqualTo(1);
        }

        @Test
        @DisplayName("la fecha de próxima revisión es consistente con el intervalo")
        void proximaRevisionConsistenteConIntervalo() {
            SustantivoFlexion flexion = crearFlexionNueva();

            service.procesarRespuesta(flexion, true);

            Instant ultima = flexion.getUltimaRevision();
            Instant proxima = flexion.getProximaRevision();
            long intervalo = flexion.getIntervaloRepeticionSegundos();

            assertThat(proxima).isEqualTo(ultima.plusSeconds(intervalo));
        }
    }

    // === Helpers ===

    private SustantivoFlexion crearFlexionNueva() {
        return SustantivoFlexion.builder()
                .id(1)
                .flexion("test")
                .build();
    }

    private SustantivoFlexion crearFlexionConUnaCorrecta() {
        return SustantivoFlexion.builder()
                .id(1)
                .vecesConsecutivasCorrectas(1)
                .factorFacilidad(2.5)
                .intervaloRepeticionSegundos(600L)
                .totalRevisiones(1)
                .totalAciertos(1)
                .build();
    }
}

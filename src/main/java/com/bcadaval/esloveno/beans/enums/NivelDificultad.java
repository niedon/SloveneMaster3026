package com.bcadaval.esloveno.beans.enums;

import lombok.Getter;

/**
 * Niveles de dificultad para las estructuras de frase.
 * Cada nivel tiene un orden numérico y un título descriptivo.
 */
@Getter
public enum NivelDificultad {
    NIVEL_1(1, "🌱 Nivel 1", "Etapa 1 - Fundamentos"),
    NIVEL_2(2, "📗 Nivel 2", "Etapa 2 - Consolidación"),
    NIVEL_3(3, "📘 Nivel 3", "Etapa 3 - Desarrollo"),
    NIVEL_4(4, "📙 Nivel 4", "Etapa 4 - Expansión"),
    NIVEL_5(5, "🎓 Nivel 5", "Etapa 5 - Fluidez"),
    NIVEL_6(6, "🌍 Nivel 6", "Etapa 6 - Siguientes");

    /**
     * Orden numérico para ordenar las secciones (0 = primero)
     */
    private final int orden;

    /**
     * Título corto para mostrar como encabezado de sección
     */
    private final String titulo;

    /**
     * Descripción explicativa del nivel
     */
    private final String descripcion;

    NivelDificultad(int orden, String titulo, String descripcion) {
        this.orden = orden;
        this.titulo = titulo;
        this.descripcion = descripcion;
    }
}

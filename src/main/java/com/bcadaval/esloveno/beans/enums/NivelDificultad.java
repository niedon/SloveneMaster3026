package com.bcadaval.esloveno.beans.enums;

import lombok.Getter;

/**
 * Niveles de dificultad para las estructuras de frase.
 * Cada nivel tiene un orden numérico y un título descriptivo.
 */
@Getter
public enum NivelDificultad {
    PRINCIPIANTE(0, "🌱 Principiante", "Estructuras básicas para empezar"),
    ELEMENTAL(1, "📗 Elemental", "Nivel A1-A2 del MCER"),
    INTERMEDIO(2, "📘 Intermedio", "Nivel B1 del MCER"),
    INTERMEDIO_ALTO(3, "📙 Intermedio Alto", "Nivel B2 del MCER"),
    AVANZADO(4, "🎓 Avanzado", "Nivel C1 del MCER"),
    MAESTRO(5, "🏆 Maestro", "Nivel C2 - Dominio completo");

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


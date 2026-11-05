package com.promou.comun;

import java.util.Locale;

public final class Texto {

    private Texto() {
    }

    public static String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    public static String recortar(String valor) {
        return valor == null ? null : valor.trim();
    }

    /** Devuelve null si el texto es nulo o está en blanco; en otro caso, el texto recortado. */
    public static String nuloSiVacio(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    public static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}

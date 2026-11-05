package com.promou.dominio.usuario;

public enum Rol {
    ESTUDIANTE("student"),
    ESTABLECIMIENTO("business"),
    ADMINISTRADOR("admin");

    private final String etiquetaFront;

    Rol(String etiquetaFront) {
        this.etiquetaFront = etiquetaFront;
    }

    /** Valor que usa el front en SessionRole. */
    public String etiquetaFront() {
        return etiquetaFront;
    }

    public String authority() {
        return "ROLE_" + name();
    }
}

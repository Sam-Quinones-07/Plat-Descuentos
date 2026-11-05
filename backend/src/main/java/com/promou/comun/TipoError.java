package com.promou.comun;

/** Categoría de error de negocio; el manejador web la traduce a un código HTTP. */
public enum TipoError {
    VALIDACION(400),
    NO_AUTENTICADO(401),
    PROHIBIDO(403),
    NO_ENCONTRADO(404),
    CONFLICTO(409),
    NO_DISPONIBLE(410),
    BLOQUEADO(423);

    private final int http;

    TipoError(int http) {
        this.http = http;
    }

    public int http() {
        return http;
    }
}

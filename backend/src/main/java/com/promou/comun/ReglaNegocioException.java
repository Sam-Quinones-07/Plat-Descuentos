package com.promou.comun;

import java.util.Map;

/**
 * Error de regla de negocio. No depende de Spring: el dominio la lanza y la capa web la traduce.
 * {@code codigo} es estable (el front puede ramificar por él); {@code mensaje} es apto para mostrar al usuario.
 */
public class ReglaNegocioException extends RuntimeException {

    private final TipoError tipo;
    private final String codigo;
    private final Map<String, String> campos;

    public ReglaNegocioException(TipoError tipo, String codigo, String mensaje, Map<String, String> campos) {
        super(mensaje);
        this.tipo = tipo;
        this.codigo = codigo;
        this.campos = campos == null ? Map.of() : Map.copyOf(campos);
    }

    public ReglaNegocioException(TipoError tipo, String codigo, String mensaje) {
        this(tipo, codigo, mensaje, null);
    }

    public static ReglaNegocioException validacion(String codigo, String mensaje) {
        return new ReglaNegocioException(TipoError.VALIDACION, codigo, mensaje);
    }

    public static ReglaNegocioException validacion(String codigo, String mensaje, Map<String, String> campos) {
        return new ReglaNegocioException(TipoError.VALIDACION, codigo, mensaje, campos);
    }

    public static ReglaNegocioException noAutenticado(String codigo, String mensaje) {
        return new ReglaNegocioException(TipoError.NO_AUTENTICADO, codigo, mensaje);
    }

    public static ReglaNegocioException prohibido(String codigo, String mensaje) {
        return new ReglaNegocioException(TipoError.PROHIBIDO, codigo, mensaje);
    }

    public static ReglaNegocioException noEncontrado(String codigo, String mensaje) {
        return new ReglaNegocioException(TipoError.NO_ENCONTRADO, codigo, mensaje);
    }

    public static ReglaNegocioException conflicto(String codigo, String mensaje) {
        return new ReglaNegocioException(TipoError.CONFLICTO, codigo, mensaje);
    }

    public static ReglaNegocioException noDisponible(String codigo, String mensaje) {
        return new ReglaNegocioException(TipoError.NO_DISPONIBLE, codigo, mensaje);
    }

    public static ReglaNegocioException bloqueado(String codigo, String mensaje) {
        return new ReglaNegocioException(TipoError.BLOQUEADO, codigo, mensaje);
    }

    public static ReglaNegocioException demasiadas(String codigo, String mensaje) {
        return new ReglaNegocioException(TipoError.DEMASIADAS_SOLICITUDES, codigo, mensaje);
    }

    public TipoError getTipo() {
        return tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}

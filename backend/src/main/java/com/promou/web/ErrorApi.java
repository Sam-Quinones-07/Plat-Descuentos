package com.promou.web;

import java.time.Instant;
import java.util.Map;

/** Cuerpo de todo error: {@code campos} se mapea directo a los errores por campo del formulario del front. */
public record ErrorApi(String codigo, String mensaje, Map<String, String> campos, Instant fecha) {

    public static ErrorApi de(String codigo, String mensaje, Map<String, String> campos) {
        return new ErrorApi(codigo, mensaje, campos == null ? Map.of() : campos, Instant.now());
    }
}

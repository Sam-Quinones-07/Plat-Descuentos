package com.promou.comun;

import java.util.regex.Pattern;

/**
 * Política de contraseñas (HU 1.1.1 esc. 6 y 7, 1.3.1, 1.3.2). Mismas reglas que el front.
 * Un único lugar para la regla: registro, cambio y restablecimiento la reutilizan.
 * El máximo de 72 caracteres es el límite real de BCrypt (trunca lo que exceda).
 */
public final class PoliticaContrasena {

    public static final int MIN = 8;
    public static final int MAX = 72;
    public static final String MENSAJE =
            "La contraseña debe tener mínimo 8 caracteres, al menos una mayúscula, una minúscula, "
                    + "un número y un carácter especial.";

    private static final Pattern MAYUSCULA = Pattern.compile("[A-Z]");
    private static final Pattern MINUSCULA = Pattern.compile("[a-z]");
    private static final Pattern NUMERO = Pattern.compile("[0-9]");
    private static final Pattern ESPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private PoliticaContrasena() {
    }

    public static boolean cumple(String contrasena) {
        return contrasena != null
                && contrasena.length() >= MIN
                && contrasena.length() <= MAX
                && MAYUSCULA.matcher(contrasena).find()
                && MINUSCULA.matcher(contrasena).find()
                && NUMERO.matcher(contrasena).find()
                && ESPECIAL.matcher(contrasena).find();
    }

    /** Lanza un error de validación con el mensaje de reglas; no revela nada sobre el resto del sistema. */
    public static void exigir(String contrasena, String campo) {
        if (!cumple(contrasena)) {
            throw ReglaNegocioException.validacion("CONTRASENA_INVALIDA", MENSAJE, java.util.Map.of(campo, MENSAJE));
        }
    }
}

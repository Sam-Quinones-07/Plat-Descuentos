package com.promou.comun;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/** Generación de valores aleatorios no adivinables y hash para almacenarlos. */
public final class CodigosSeguros {

    private static final SecureRandom RANDOM = new SecureRandom();
    /** Alfabeto Crockford base32: sin I, L, O, U para evitar confusiones al leer el código. */
    private static final char[] ALFABETO = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final int LONGITUD_CREDENCIAL = 10; // 50 bits de entropía

    private CodigosSeguros() {
    }

    /** Token opaco de 256 bits (sesiones y enlaces de correo). Se entrega al usuario; en BD solo va su hash. */
    public static String tokenOpaco() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Código de la identificación digital, p. ej. {@code U-7K3M0Q9ZTA}. Es el contenido del QR. */
    public static String codigoCredencial() {
        StringBuilder sb = new StringBuilder("U-");
        for (int i = 0; i < LONGITUD_CREDENCIAL; i++) {
            sb.append(ALFABETO[RANDOM.nextInt(ALFABETO.length)]);
        }
        return sb.toString();
    }

    public static String sha256Hex(String valor) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}

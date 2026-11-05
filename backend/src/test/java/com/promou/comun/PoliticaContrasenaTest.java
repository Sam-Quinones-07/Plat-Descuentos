package com.promou.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PoliticaContrasenaTest {

    @Test
    void aceptaContrasenaQueCumpleTodasLasReglas() {
        assertTrue(PoliticaContrasena.cumple("Universitario@26"));
        assertTrue(PoliticaContrasena.cumple("Descuentos@04"));
    }

    @Test
    void rechazaCadaReglaIncumplida() {
        assertFalse(PoliticaContrasena.cumple("Ab1@xyz"), "menos de 8 caracteres");
        assertFalse(PoliticaContrasena.cumple("universitario@26"), "sin mayúscula");
        assertFalse(PoliticaContrasena.cumple("UNIVERSITARIO@26"), "sin minúscula");
        assertFalse(PoliticaContrasena.cumple("Universitario@@"), "sin número");
        assertFalse(PoliticaContrasena.cumple("Universitario26"), "sin carácter especial");
        assertFalse(PoliticaContrasena.cumple(null));
    }

    @Test
    void rechazaMasDe72CaracteresPorElLimiteDeBcrypt() {
        assertFalse(PoliticaContrasena.cumple("Aa1@" + "x".repeat(70)));
        assertTrue(PoliticaContrasena.cumple("Aa1@" + "x".repeat(68)));
    }

    @Test
    void exigirLanzaMensajeConLasReglasSinDetallesInternos() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> PoliticaContrasena.exigir("abc", "password"));
        assertEquals("CONTRASENA_INVALIDA", ex.getCodigo());
        assertEquals(PoliticaContrasena.MENSAJE, ex.getCampos().get("password"));
    }
}

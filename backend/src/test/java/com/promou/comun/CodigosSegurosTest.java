package com.promou.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CodigosSegurosTest {

    @Test
    void codigoDeCredencialTieneFormatoYEsDistintoCadaVez() {
        String a = CodigosSeguros.codigoCredencial();
        String b = CodigosSeguros.codigoCredencial();
        assertTrue(a.matches("U-[0-9A-HJKMNP-TV-Z]{10}"), a);
        assertNotEquals(a, b);
    }

    @Test
    void tokenOpacoEsLargoYUrlSafe() {
        String t = CodigosSeguros.tokenOpaco();
        assertEquals(43, t.length());
        assertTrue(t.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void sha256ConocidoParaAbc() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", CodigosSeguros.sha256Hex("abc"));
    }
}

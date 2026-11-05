package com.promou.comun;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DominioInstitucionalTest {

    private final DominioInstitucional dominios = new DominioInstitucional(List.of("udea.edu.co", "unal.edu.co"));

    @Test
    void aceptaDominioExactoYSubdominios() {
        assertTrue(dominios.esInstitucional("mateo@udea.edu.co"));
        assertTrue(dominios.esInstitucional("ana@est.unal.edu.co"));
    }

    @Test
    void rechazaDominiosParecidosOAjenos() {
        assertFalse(dominios.esInstitucional("mateo@gmail.com"));
        assertFalse(dominios.esInstitucional("mateo@xudea.edu.co"));
        assertFalse(dominios.esInstitucional("mateo@udea.edu.co.atacante.com"));
        assertFalse(dominios.esInstitucional("mateo@evil-udea.edu.co"));
    }

    @Test
    void rechazaCorreosMalFormados() {
        assertFalse(dominios.esInstitucional("@udea.edu.co"));
        assertFalse(dominios.esInstitucional("udea.edu.co"));
        assertFalse(dominios.esInstitucional("mateo@"));
    }
}

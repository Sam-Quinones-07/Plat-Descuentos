package com.promou.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.resena.Resena;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** HU 3.1.2 esc. 3: no se publica una reseña sin contenido. */
class ResenaTest {

    private static final Instant AHORA = Instant.parse("2026-11-20T10:00:00Z");

    @Test
    void resenaConSoloEspaciosNoSePublica() {
        assertEquals("RESENA_VACIA", assertThrows(ReglaNegocioException.class,
                () -> new Resena(null, null, "   ", AHORA).validar()).getCodigo());
    }

    @Test
    void resenaDeMasDe500CaracteresNoSePublica() {
        assertEquals("RESENA_LARGA", assertThrows(ReglaNegocioException.class,
                () -> new Resena(null, null, "a".repeat(501), AHORA).validar()).getCodigo());
    }
}

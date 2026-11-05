package com.promou.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.credencial.CredencialDigital;
import com.promou.dominio.usuario.Estudiante;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** HU 1.4.1: el estudiante verificado crea su identificación digital (Creator). */
class CredencialTest {

    private static final Instant AHORA = Instant.parse("2026-10-26T12:00:00Z");

    private Estudiante estudiante() {
        return new Estudiante("mateo@udea.edu.co", "hash", "Mateo", "Gómez", "UdeA", AHORA);
    }

    @Test
    void unEstudianteVerificadoGeneraSuCredencialVigente() {
        Estudiante e = estudiante();
        e.confirmarCorreo();
        CredencialDigital c = e.generarCredencial("U-ABCDEFGHJK", AHORA, Duration.ofDays(365));
        assertSame(e, c.getEstudiante());
        assertEquals("U-ABCDEFGHJK", c.getCodigoQr());
        assertTrue(c.validar(AHORA.plusSeconds(60)));
        assertFalse(c.validar(AHORA.plus(Duration.ofDays(366))));
    }

    @Test
    void unEstudianteNoVerificadoNoObtieneCredencial() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> estudiante().generarCredencial("U-ABCDEFGHJK", AHORA, Duration.ofDays(365)));
        assertEquals("ESTUDIANTE_NO_VERIFICADO", ex.getCodigo());
    }
}

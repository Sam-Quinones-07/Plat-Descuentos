package com.promou.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.cuenta.TokenVerificacion;
import com.promou.dominio.usuario.Estudiante;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** HU 1.1.2 y 1.2.2: enlaces de confirmación de un solo uso con vencimiento de 24 h. */
class TokenVerificacionTest {

    private static final Instant EMISION = Instant.parse("2026-11-20T10:00:00Z");

    private TokenVerificacion token(Estudiante e) {
        return TokenVerificacion.emitir(e, TokenVerificacion.Tipo.CONFIRMACION, "hash", EMISION, Duration.ofHours(24));
    }

    private Estudiante estudiante() {
        return new Estudiante("laura@udea.edu.co", "hash", "Laura", "Gómez", "UdeA", EMISION);
    }

    @Test
    void enlaceValidoConfirmaLaCuentaYQuedaUsado() {
        Estudiante e = estudiante();
        TokenVerificacion t = token(e);
        t.confirmar(EMISION.plus(Duration.ofHours(2)));
        assertTrue(e.isCorreoConfirmado());
        assertTrue(e.isEstadoVerificado());
        assertNotNull(t.getUsadoEn());
    }

    @Test
    void enlaceExpiradoNoConfirma() {
        Estudiante e = estudiante();
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> token(e).confirmar(EMISION.plus(Duration.ofHours(25))));
        assertEquals("ENLACE_INVALIDO", ex.getCodigo());
        assertFalse(e.isCorreoConfirmado());
    }

    @Test
    void cuentaYaConfirmadaNoRepiteElProceso() {
        Estudiante e = estudiante();
        TokenVerificacion t = token(e);
        t.confirmar(EMISION.plusSeconds(60));
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> t.confirmar(EMISION.plusSeconds(120)));
        assertEquals("CUENTA_YA_CONFIRMADA", ex.getCodigo());
    }

    @Test
    void enlaceDeRestablecimientoSoloSirveUnaVez() {
        TokenVerificacion t = TokenVerificacion.emitir(estudiante(), TokenVerificacion.Tipo.RESTABLECIMIENTO, "h",
                EMISION, Duration.ofHours(1));
        t.consumir(EMISION.plusSeconds(60));
        assertEquals("ENLACE_INVALIDO",
                assertThrows(ReglaNegocioException.class, () -> t.consumir(EMISION.plusSeconds(90))).getCodigo());
        assertFalse(t.vigente(EMISION.plusSeconds(90)));
    }
}

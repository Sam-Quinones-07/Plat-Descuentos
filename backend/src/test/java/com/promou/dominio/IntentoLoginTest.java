package com.promou.dominio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.promou.dominio.cuenta.IntentoLogin;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class IntentoLoginTest {

    private static final Duration VENTANA = Duration.ofMinutes(15);
    private static final Duration BLOQUEO = Duration.ofMinutes(15);
    private static final Instant T0 = Instant.parse("2026-10-26T12:00:00Z");

    @Test
    void elQuintoFalloSeguidoBloqueaPorQuinceMinutos() {
        IntentoLogin i = new IntentoLogin("k");
        for (int n = 0; n < 4; n++) {
            assertFalse(i.registrarFallo(T0.plusSeconds(n), 5, VENTANA, BLOQUEO));
            assertFalse(i.bloqueado(T0.plusSeconds(n)));
        }
        assertTrue(i.registrarFallo(T0.plusSeconds(4), 5, VENTANA, BLOQUEO));
        assertTrue(i.bloqueado(T0.plusSeconds(5)));
        assertTrue(i.bloqueado(T0.plus(Duration.ofMinutes(14))));
        assertFalse(i.bloqueado(T0.plusSeconds(4).plus(BLOQUEO)));
    }

    @Test
    void losFallosFueraDeLaVentanaNoSeAcumulan() {
        IntentoLogin i = new IntentoLogin("k");
        for (int n = 0; n < 4; n++) {
            i.registrarFallo(T0, 5, VENTANA, BLOQUEO);
        }
        // 16 minutos después la ventana expiró: este fallo cuenta como el primero
        assertFalse(i.registrarFallo(T0.plus(Duration.ofMinutes(16)), 5, VENTANA, BLOQUEO));
        assertFalse(i.bloqueado(T0.plus(Duration.ofMinutes(16))));
    }

    @Test
    void despuesDelBloqueoElContadorEmpiezaDeCero() {
        IntentoLogin i = new IntentoLogin("k");
        for (int n = 0; n < 5; n++) {
            i.registrarFallo(T0, 5, VENTANA, BLOQUEO);
        }
        Instant despues = T0.plus(Duration.ofMinutes(16));
        assertFalse(i.bloqueado(despues));
        assertFalse(i.registrarFallo(despues, 5, VENTANA, BLOQUEO));
    }
}

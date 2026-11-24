package com.promou.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.usuario.Estudiante;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** HU 1.3.1 (cambiar) y 1.3.2 (restablecer): reglas de la contraseña en el usuario. */
class UsuarioContrasenaTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    private Estudiante estudiante() {
        return new Estudiante("laura@udea.edu.co", encoder.encode("Universitario@26"), "Laura", "Gómez", "UdeA",
                Instant.parse("2026-11-20T10:00:00Z"));
    }

    @Test
    void contrasenaActualIncorrectaNoCambiaNada() {
        Estudiante e = estudiante();
        String hashAntes = e.getContrasenaHash();
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> e.cambiarContrasena("Equivocada@1", "Nueva@2026", encoder));
        assertEquals("CONTRASENA_ACTUAL_INCORRECTA", ex.getCodigo());
        assertEquals(hashAntes, e.getContrasenaHash());
    }

    @Test
    void cambioExitosoReemplazaElHash() {
        Estudiante e = estudiante();
        e.cambiarContrasena("Universitario@26", "Nueva@2026", encoder);
        assertTrue(encoder.matches("Nueva@2026", e.getContrasenaHash()));
        assertFalse(encoder.matches("Universitario@26", e.getContrasenaHash()));
    }

    @Test
    void laNuevaContrasenaDebeCumplirLaPolitica() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> estudiante().cambiarContrasena("Universitario@26", "debil", encoder));
        assertEquals("CONTRASENA_INVALIDA", ex.getCodigo());
        assertTrue(ex.getCampos().containsKey("newPassword"));
    }

    @Test
    void restablecerInvalidaLaContrasenaAnterior() {
        Estudiante e = estudiante();
        e.restablecerContrasena("Otra@2026x", encoder);
        assertTrue(encoder.matches("Otra@2026x", e.getContrasenaHash()));
        assertFalse(encoder.matches("Universitario@26", e.getContrasenaHash()));
    }
}

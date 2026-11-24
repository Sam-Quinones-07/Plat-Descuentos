package com.promou.dominio.usuario;

import com.promou.comun.PoliticaContrasena;
import com.promou.comun.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Abstracción común de autenticación (diagrama de clases 4.2): Estudiante, Establecimiento y Administrador heredan de aquí. */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 254)
    private String correo;

    @Column(name = "contrasena_hash", nullable = false, length = 100)
    private String contrasenaHash;

    @Column(name = "correo_confirmado", nullable = false)
    private boolean correoConfirmado;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;

    /** Prueba de la autorización de tratamiento de datos (sección 11.1, Ley 1581 de 2012). */
    @Column(name = "terminos_aceptados_en")
    private Instant terminosAceptadosEn;

    @Column(name = "terminos_version", length = 20)
    private String terminosVersion;

    protected Usuario() {
    }

    protected Usuario(String correo, String contrasenaHash, Instant ahora) {
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.fechaRegistro = ahora;
    }

    public abstract Rol getRol();

    public abstract String getNombreVisible();

    public void confirmarCorreo() {
        this.correoConfirmado = true;
    }

    public void aceptarTerminos(String version, Instant ahora) {
        this.terminosVersion = version;
        this.terminosAceptadosEn = ahora;
    }

    /**
     * HU 1.3.1: exige la contraseña actual (esc. 2) y que la nueva cumpla la política (esc. 4).
     * Information Expert: el usuario tiene el hash, así que él decide si la contraseña actual coincide.
     */
    public void cambiarContrasena(String actual, String nueva, PasswordEncoder encoder) {
        if (actual == null || !encoder.matches(actual, contrasenaHash)) {
            throw ReglaNegocioException.validacion("CONTRASENA_ACTUAL_INCORRECTA",
                    "La contraseña actual no es correcta.",
                    Map.of("currentPassword", "La contraseña actual no es correcta."));
        }
        PoliticaContrasena.exigir(nueva, "newPassword");
        if (encoder.matches(nueva, contrasenaHash)) {
            throw ReglaNegocioException.validacion("CONTRASENA_REPETIDA",
                    "La nueva contraseña debe ser distinta de la actual.",
                    Map.of("newPassword", "La nueva contraseña debe ser distinta de la actual."));
        }
        this.contrasenaHash = encoder.encode(nueva);
    }

    /** HU 1.3.2: el restablecimiento ya validó el enlace, así que reemplaza la contraseña sin pedir la anterior. */
    public void restablecerContrasena(String nueva, PasswordEncoder encoder) {
        PoliticaContrasena.exigir(nueva, "password");
        this.contrasenaHash = encoder.encode(nueva);
    }

    /** Método plantilla: cada tipo de usuario añade sus propias condiciones sin condicionales externos. */
    public void verificarPuedeIniciarSesion() {
        if (!correoConfirmado) {
            throw ReglaNegocioException.prohibido("CORREO_NO_CONFIRMADO",
                    "Debes confirmar tu correo electrónico antes de iniciar sesión.");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public boolean isCorreoConfirmado() {
        return correoConfirmado;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public Instant getTerminosAceptadosEn() {
        return terminosAceptadosEn;
    }

    public String getTerminosVersion() {
        return terminosVersion;
    }
}

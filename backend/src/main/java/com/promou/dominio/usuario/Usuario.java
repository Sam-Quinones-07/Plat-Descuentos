package com.promou.dominio.usuario;

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
import java.util.UUID;

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

    public void cambiarContrasena(String nuevoHash) {
        this.contrasenaHash = nuevoHash;
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
}

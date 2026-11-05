package com.promou.dominio.cuenta;

import com.promou.dominio.usuario.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Sesión del lado del servidor: cerrar sesión la elimina (HU 1.1.4 esc. 4) y expira por inactividad (esc. 7). */
@Entity
@Table(name = "sesion")
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    @Column(name = "ultima_actividad", nullable = false)
    private Instant ultimaActividad;

    protected Sesion() {
    }

    public Sesion(String tokenHash, UUID usuarioId, Rol rol, Instant ahora) {
        this.tokenHash = tokenHash;
        this.usuarioId = usuarioId;
        this.rol = rol;
        this.creadaEn = ahora;
        this.ultimaActividad = ahora;
    }

    public boolean expiro(Instant ahora, Duration inactividad) {
        return !ahora.isBefore(ultimaActividad.plus(inactividad));
    }

    /** Renueva la actividad; evita escribir en BD en cada petición (margen de 30 s). */
    public void registrarActividad(Instant ahora) {
        if (Duration.between(ultimaActividad, ahora).compareTo(Duration.ofSeconds(30)) > 0) {
            ultimaActividad = ahora;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public Rol getRol() {
        return rol;
    }
}

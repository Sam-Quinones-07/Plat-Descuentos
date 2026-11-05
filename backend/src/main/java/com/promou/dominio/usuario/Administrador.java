package com.promou.dominio.usuario;

import com.promou.dominio.afiliacion.SolicitudAfiliacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "administrador")
public class Administrador extends Usuario {

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "nivel_acceso", nullable = false)
    private int nivelAcceso;

    protected Administrador() {
    }

    public Administrador(String correo, String contrasenaHash, String nombre, int nivelAcceso, Instant ahora) {
        super(correo, contrasenaHash, ahora);
        this.nombre = nombre;
        this.nivelAcceso = nivelAcceso;
        confirmarCorreo();
    }

    /** revisarSolicitud(est, aprobado) del diagrama de clases 4.2. */
    public void revisarSolicitud(Establecimiento establecimiento, boolean aprobado, String observaciones, Instant ahora) {
        SolicitudAfiliacion solicitud = establecimiento.getSolicitud();
        if (aprobado) {
            solicitud.aprobar(this, observaciones, ahora);
        } else {
            solicitud.rechazar(this, observaciones, ahora);
        }
    }

    @Override
    public Rol getRol() {
        return Rol.ADMINISTRADOR;
    }

    @Override
    public String getNombreVisible() {
        return nombre;
    }

    public int getNivelAcceso() {
        return nivelAcceso;
    }
}

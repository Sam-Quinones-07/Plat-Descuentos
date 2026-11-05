package com.promou.dominio.usuario;

import com.promou.dominio.credencial.CredencialDigital;
import jakarta.persistence.Column;
import java.time.Duration;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "estudiante")
public class Estudiante extends Usuario {

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    @Column(nullable = false, length = 120)
    private String universidad;

    @Column(name = "estado_verificado", nullable = false)
    private boolean estadoVerificado;

    protected Estudiante() {
    }

    public Estudiante(String correo, String contrasenaHash, String nombre, String apellido, String universidad,
                      Instant ahora) {
        super(correo, contrasenaHash, ahora);
        this.nombre = nombre;
        this.apellido = apellido;
        this.universidad = universidad;
    }

    /** Confirmar el correo institucional es lo que verifica la condición de estudiante (HU 1.4.1). */
    @Override
    public void confirmarCorreo() {
        super.confirmarCorreo();
        this.estadoVerificado = true;
    }

    /**
     * GRASP Creator (HU 1.4.1): el estudiante es dueño de su identificación digital, por eso es quien la crea.
     * Solo un estudiante verificado puede obtenerla (esc. 3).
     */
    public CredencialDigital generarCredencial(String codigoQr, Instant ahora, Duration vigencia) {
        if (!estadoVerificado) {
            throw com.promou.comun.ReglaNegocioException.prohibido("ESTUDIANTE_NO_VERIFICADO",
                    "Debes completar la verificación de tu condición de estudiante.");
        }
        return CredencialDigital.emitir(this, codigoQr, ahora, vigencia);
    }

    @Override
    public Rol getRol() {
        return Rol.ESTUDIANTE;
    }

    @Override
    public String getNombreVisible() {
        return nombre + " " + apellido;
    }

    /** "Mateo G." — lo que ve un comercio al validar un QR, sin exponer el nombre completo. */
    public String getNombreCorto() {
        return nombre + " " + apellido.charAt(0) + ".";
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getUniversidad() {
        return universidad;
    }

    public boolean isEstadoVerificado() {
        return estadoVerificado;
    }
}

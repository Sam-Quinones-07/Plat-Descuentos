package com.promou.dominio.cuenta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;

/** Intentos fallidos por correo (HU 1.1.3 esc. 6): 5 fallos seguidos en 15 min bloquean 15 min. */
@Entity
@Table(name = "intento_login")
public class IntentoLogin {

    @Id
    @Column(name = "clave_hash", length = 64)
    private String claveHash;

    @Column(nullable = false)
    private int intentos;

    @Column(name = "ventana_inicio")
    private Instant ventanaInicio;

    @Column(name = "bloqueado_hasta")
    private Instant bloqueadoHasta;

    protected IntentoLogin() {
    }

    public IntentoLogin(String claveHash) {
        this.claveHash = claveHash;
    }

    /** @return true si este fallo dispara el bloqueo. */
    public boolean registrarFallo(Instant ahora, int maximo, Duration ventana, Duration bloqueo) {
        if (ventanaInicio == null || !ahora.isBefore(ventanaInicio.plus(ventana))) {
            intentos = 0;
            ventanaInicio = ahora;
        }
        intentos++;
        if (intentos >= maximo) {
            bloqueadoHasta = ahora.plus(bloqueo);
            intentos = 0;
            ventanaInicio = null;
            return true;
        }
        return false;
    }

    public boolean bloqueado(Instant ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    public int getIntentos() {
        return intentos;
    }
}

package com.promou.dominio.afiliacion;

import com.promou.dominio.usuario.Administrador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "solicitud_afiliacion")
public class SolicitudAfiliacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private Instant fechaSolicitud;

    @Column(name = "fecha_resolucion")
    private Instant fechaResolucion;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrador_id")
    private Administrador administrador;

    @Version
    private long version;

    protected SolicitudAfiliacion() {
    }

    public static SolicitudAfiliacion pendiente(Instant ahora) {
        SolicitudAfiliacion s = new SolicitudAfiliacion();
        s.estado = EstadoSolicitud.PENDIENTE;
        s.fechaSolicitud = ahora;
        return s;
    }

    public void aprobar(Administrador admin, String observaciones, Instant ahora) {
        resolver(estado.aprobar(), admin, observaciones, ahora);
    }

    public void rechazar(Administrador admin, String observaciones, Instant ahora) {
        resolver(estado.rechazar(), admin, observaciones, ahora);
    }

    private void resolver(EstadoSolicitud nuevo, Administrador admin, String observaciones, Instant ahora) {
        this.estado = nuevo;
        this.administrador = admin;
        this.observaciones = observaciones;
        this.fechaResolucion = ahora;
    }

    public UUID getId() {
        return id;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public Instant getFechaSolicitud() {
        return fechaSolicitud;
    }

    public Instant getFechaResolucion() {
        return fechaResolucion;
    }

    public String getObservaciones() {
        return observaciones;
    }
}

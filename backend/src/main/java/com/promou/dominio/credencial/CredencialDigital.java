package com.promou.dominio.credencial;

import com.promou.dominio.usuario.Estudiante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Identificación digital con código QR (HU 1.4.1 / 1.4.2). Responsabilidad única: la lógica del QR (SRP). */
@Entity
@Table(name = "credencial_digital")
public class CredencialDigital {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(name = "codigo_qr", nullable = false, length = 40)
    private String codigoQr;

    @Column(name = "fecha_emision", nullable = false)
    private Instant fechaEmision;

    @Column(name = "fecha_vencimiento", nullable = false)
    private Instant fechaVencimiento;

    @Column(nullable = false)
    private boolean revocada;

    protected CredencialDigital() {
    }

    /** generar(est) del diagrama de clases. */
    public static CredencialDigital emitir(Estudiante estudiante, String codigoQr, Instant ahora, Duration vigencia) {
        CredencialDigital c = new CredencialDigital();
        c.estudiante = estudiante;
        c.codigoQr = codigoQr;
        c.fechaEmision = ahora;
        c.fechaVencimiento = ahora.plus(vigencia);
        return c;
    }

    /** validar(): vigente si no está revocada, no venció y el estudiante sigue verificado. */
    public boolean validar(Instant ahora) {
        return !revocada && ahora.isBefore(fechaVencimiento) && estudiante.isEstadoVerificado();
    }

    public UUID getId() {
        return id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public String getCodigoQr() {
        return codigoQr;
    }

    public Instant getFechaEmision() {
        return fechaEmision;
    }

    public Instant getFechaVencimiento() {
        return fechaVencimiento;
    }

    public boolean isRevocada() {
        return revocada;
    }
}

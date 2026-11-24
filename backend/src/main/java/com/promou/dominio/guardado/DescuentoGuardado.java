package com.promou.dominio.guardado;

import com.promou.dominio.descuento.Descuento;
import com.promou.dominio.usuario.Estudiante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** HU 2.2.4: un descuento que el estudiante guardó para usarlo después. */
@Entity
@Table(name = "descuento_guardado")
public class DescuentoGuardado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "descuento_id", nullable = false)
    private Descuento descuento;

    @Column(name = "guardado_en", nullable = false)
    private Instant guardadoEn;

    protected DescuentoGuardado() {
    }

    public DescuentoGuardado(Estudiante estudiante, Descuento descuento, Instant ahora) {
        this.estudiante = estudiante;
        this.descuento = descuento;
        this.guardadoEn = ahora;
    }

    public UUID getId() {
        return id;
    }

    public Descuento getDescuento() {
        return descuento;
    }

    public Instant getGuardadoEn() {
        return guardadoEn;
    }
}

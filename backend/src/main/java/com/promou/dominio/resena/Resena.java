package com.promou.dominio.resena;

import com.promou.comun.ReglaNegocioException;
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
import java.util.Map;
import java.util.UUID;

/**
 * HU 3.1.1 y 3.1.2. Texto plano con longitud máxima: el front lo muestra escapado (sección 7.5).
 * Information Expert: la reseña valida su propio contenido.
 */
@Entity
@Table(name = "resena")
public class Resena {

    public static final int MAX = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "descuento_id", nullable = false)
    private Descuento descuento;

    @Column(nullable = false, length = MAX)
    private String texto;

    @Column(name = "publicada_en", nullable = false)
    private Instant publicadaEn;

    @Column(nullable = false)
    private boolean retirada;

    protected Resena() {
    }

    public Resena(Estudiante estudiante, Descuento descuento, String texto, Instant ahora) {
        this.estudiante = estudiante;
        this.descuento = descuento;
        this.texto = texto == null ? null : texto.trim();
        this.publicadaEn = ahora;
    }

    /** Esc. 3: no se publica una reseña vacía. */
    public void validar() {
        if (texto == null || texto.isBlank()) {
            throw ReglaNegocioException.validacion("RESENA_VACIA", "Debes escribir el contenido de la reseña.",
                    Map.of("text", "Debes escribir el contenido de la reseña."));
        }
        if (texto.length() > MAX) {
            throw ReglaNegocioException.validacion("RESENA_LARGA", "La reseña no puede superar 500 caracteres.",
                    Map.of("text", "La reseña no puede superar 500 caracteres."));
        }
    }

    /** El administrador retira reseñas ofensivas o falsas (deuda técnica 10). */
    public void retirar() {
        retirada = true;
    }

    public UUID getId() {
        return id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public Descuento getDescuento() {
        return descuento;
    }

    public String getTexto() {
        return texto;
    }

    public Instant getPublicadaEn() {
        return publicadaEn;
    }

    public boolean isRetirada() {
        return retirada;
    }
}

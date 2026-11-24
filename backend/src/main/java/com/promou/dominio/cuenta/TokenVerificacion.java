package com.promou.dominio.cuenta;

import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Enlace de un solo uso enviado por correo (HU 1.1.2, 1.2.2 y 1.3.2). En la base solo queda el hash del token.
 * Information Expert: el token sabe si venció o ya se usó, así que él decide si se puede consumir.
 */
@Entity
@Table(name = "token_verificacion")
public class TokenVerificacion {

    public enum Tipo { CONFIRMACION, RESTABLECIMIENTO }

    static final String MENSAJE_INVALIDO = "El enlace ya no es válido. Solicita uno nuevo.";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    @Column(name = "usado_en")
    private Instant usadoEn;

    protected TokenVerificacion() {
    }

    public static TokenVerificacion emitir(Usuario usuario, Tipo tipo, String tokenHash, Instant ahora,
                                           Duration vigencia) {
        TokenVerificacion t = new TokenVerificacion();
        t.usuario = usuario;
        t.tipo = tipo;
        t.tokenHash = tokenHash;
        t.creadoEn = ahora;
        t.expiraEn = ahora.plus(vigencia);
        return t;
    }

    /**
     * HU 1.1.2 / 1.2.2: confirma el correo del usuario.
     * Esc. 4: un enlace ya usado indica que la cuenta ya está confirmada y no repite el proceso.
     * Esc. 3: un enlace vencido no confirma nada.
     */
    public void confirmar(Instant ahora) {
        if (usadoEn != null || usuario.isCorreoConfirmado()) {
            throw ReglaNegocioException.conflicto("CUENTA_YA_CONFIRMADA", "Tu cuenta ya se encuentra confirmada.");
        }
        exigirVigente(ahora);
        usuario.confirmarCorreo();
        usadoEn = ahora;
    }

    /** HU 1.3.2 esc. 4 y 5: un enlace vencido o ya usado responde con el mismo mensaje genérico. */
    public void consumir(Instant ahora) {
        if (usadoEn != null) {
            throw ReglaNegocioException.validacion("ENLACE_INVALIDO",
                    "No es posible completar el proceso. Solicita un nuevo enlace.");
        }
        exigirVigente(ahora);
        usadoEn = ahora;
    }

    public boolean vigente(Instant ahora) {
        return usadoEn == null && ahora.isBefore(expiraEn);
    }

    private void exigirVigente(Instant ahora) {
        if (!ahora.isBefore(expiraEn)) {
            throw ReglaNegocioException.validacion("ENLACE_INVALIDO", MENSAJE_INVALIDO);
        }
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Instant getUsadoEn() {
        return usadoEn;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }
}

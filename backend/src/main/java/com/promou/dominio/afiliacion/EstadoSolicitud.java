package com.promou.dominio.afiliacion;

import com.promou.comun.ReglaNegocioException;

/**
 * Patrón State: cada estado decide qué transiciones permite (HU 1.2.3 esc. 5: una solicitud ya revisada
 * no se revisa de nuevo) y si el establecimiento puede iniciar sesión (HU 1.2.4).
 */
public enum EstadoSolicitud {

    PENDIENTE("pending") {
        @Override
        public EstadoSolicitud aprobar() {
            return APROBADA;
        }

        @Override
        public EstadoSolicitud rechazar() {
            return RECHAZADA;
        }

        @Override
        public void verificarAccesoLogin() {
            throw ReglaNegocioException.prohibido("AFILIACION_PENDIENTE",
                    "Tu solicitud de afiliación está pendiente de revisión por el administrador.");
        }
    },
    APROBADA("approved") {
        @Override
        public EstadoSolicitud aprobar() {
            throw yaRevisada(this);
        }

        @Override
        public EstadoSolicitud rechazar() {
            throw yaRevisada(this);
        }

        @Override
        public void verificarAccesoLogin() {
            // acceso permitido
        }
    },
    RECHAZADA("rejected") {
        @Override
        public EstadoSolicitud aprobar() {
            throw yaRevisada(this);
        }

        @Override
        public EstadoSolicitud rechazar() {
            throw yaRevisada(this);
        }

        @Override
        public void verificarAccesoLogin() {
            throw ReglaNegocioException.prohibido("AFILIACION_RECHAZADA", "Estado de la solicitud: Rechazada.");
        }
    };

    private final String etiquetaFront;

    EstadoSolicitud(String etiquetaFront) {
        this.etiquetaFront = etiquetaFront;
    }

    public abstract EstadoSolicitud aprobar();

    public abstract EstadoSolicitud rechazar();

    public abstract void verificarAccesoLogin();

    public boolean revisada() {
        return this != PENDIENTE;
    }

    /** pending | approved | rejected, como BusinessAccount.status en el front. */
    public String etiquetaFront() {
        return etiquetaFront;
    }

    private static ReglaNegocioException yaRevisada(EstadoSolicitud actual) {
        return ReglaNegocioException.conflicto("SOLICITUD_YA_REVISADA",
                "La solicitud ya fue revisada. Estado actual: " + (actual == APROBADA ? "aprobada" : "rechazada") + ".");
    }
}

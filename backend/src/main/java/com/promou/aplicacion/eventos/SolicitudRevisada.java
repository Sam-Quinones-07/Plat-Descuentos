package com.promou.aplicacion.eventos;

import com.promou.dominio.afiliacion.EstadoSolicitud;

/** Se publica cuando el administrador aprueba o rechaza una solicitud de afiliación (HU 1.2.3 esc. 6). */
public record SolicitudRevisada(String correoRepresentante, String nombreRepresentante, String nombreComercial,
                                EstadoSolicitud estado, String observaciones) {
}

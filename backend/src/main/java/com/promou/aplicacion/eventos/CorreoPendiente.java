package com.promou.aplicacion.eventos;

import com.promou.aplicacion.notificacion.NotificacionService.Mensaje;

/** Correo que debe salir cuando la transacción que lo originó se confirme (enlaces de confirmación y restablecimiento). */
public record CorreoPendiente(Mensaje mensaje) {
}

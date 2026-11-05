package com.promou.seguridad;

import com.promou.dominio.usuario.Rol;
import java.util.UUID;

/** Principal de la petición autenticada. */
public record UsuarioAutenticado(UUID usuarioId, UUID sesionId, Rol rol) {
}

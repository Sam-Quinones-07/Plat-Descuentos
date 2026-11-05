package com.promou.aplicacion;

import com.promou.aplicacion.dto.AuthDtos.PerfilDto;
import com.promou.aplicacion.dto.DescuentoDtos.DescuentoResponse;
import com.promou.dominio.descuento.Descuento;
import com.promou.dominio.usuario.Establecimiento;
import com.promou.dominio.usuario.Estudiante;
import com.promou.dominio.usuario.Usuario;
import java.time.LocalDate;

/** Entidad -> DTO. Se invoca siempre dentro de la transacción del servicio (hay relaciones perezosas). */
public final class Mapeos {

    private Mapeos() {
    }

    public static PerfilDto perfil(Usuario u) {
        String rol = u.getRol().etiquetaFront();
        if (u instanceof Estudiante e) {
            return new PerfilDto(rol, e.getCorreo(), e.getNombre(), e.getApellido(), e.getUniversidad(), null, null);
        }
        if (u instanceof Establecimiento s) {
            return new PerfilDto(rol, s.getCorreo(), null, null, null, s.getNombreComercial(),
                    s.getEstadoAfiliacion().etiquetaFront());
        }
        return new PerfilDto(rol, u.getCorreo(), u.getNombreVisible(), null, null, null, null);
    }

    /** El código solo lo ve el establecimiento dueño; el estudiante lo verá con la HU 2.2.5 (Release 2). */
    public static DescuentoResponse descuento(Descuento d, LocalDate hoy, boolean esDueno) {
        return new DescuentoResponse(d.getId(), d.getEstablecimiento().getNombreComercial(), d.getBeneficio(),
                d.getCondiciones(), d.getFechaInicio(), d.getFechaFin(), esDueno ? d.getCodigo() : null,
                d.getUbicacion(), d.getSitioWeb(), d.getCategoria().getId(), d.isActivo() ? "active" : "inactive",
                d.estaDisponible(hoy));
    }
}

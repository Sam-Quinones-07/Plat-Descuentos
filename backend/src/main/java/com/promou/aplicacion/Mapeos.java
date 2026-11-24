package com.promou.aplicacion;

import com.promou.aplicacion.dto.AuthDtos.PerfilDto;
import com.promou.aplicacion.dto.DescuentoDtos.DescuentoResponse;
import com.promou.aplicacion.dto.DescuentoDtos.ResenaResponse;
import com.promou.dominio.resena.Resena;
import com.promou.dominio.descuento.Descuento;
import com.promou.dominio.usuario.Establecimiento;
import com.promou.dominio.usuario.Estudiante;
import com.promou.dominio.usuario.Usuario;
import java.time.LocalDate;
import java.util.UUID;

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

    /** El dueño ve siempre su código; el estudiante solo cuando se puede copiar (HU 2.2.5 esc. 2 y 4). */
    public static DescuentoResponse descuento(Descuento d, LocalDate hoy, boolean esDueno) {
        String codigo = esDueno ? d.getCodigo() : d.codigoCopiable(hoy).orElse(null);
        return new DescuentoResponse(d.getId(), d.getEstablecimiento().getNombreComercial(), d.getBeneficio(),
                d.getCondiciones(), d.getFechaInicio(), d.getFechaFin(), codigo, d.getUbicacion(), d.getSitioWeb(),
                d.getCategoria().getId(), d.isActivo() ? "active" : "inactive", d.estaDisponible(hoy), null);
    }

    public static ResenaResponse resena(Resena r, UUID estudianteActual) {
        return new ResenaResponse(r.getId(), r.getEstudiante().getNombreCorto(), r.getTexto(), r.getPublicadaEn(),
                estudianteActual != null && estudianteActual.equals(r.getEstudiante().getId()) ? Boolean.TRUE : null);
    }
}

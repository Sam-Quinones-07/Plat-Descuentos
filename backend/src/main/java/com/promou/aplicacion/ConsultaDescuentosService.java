package com.promou.aplicacion;

import com.promou.aplicacion.dto.DescuentoDtos.DescuentoResponse;
import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.descuento.Descuento;
import com.promou.dominio.descuento.DescuentoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU 2.2.1 (directorio) y 2.2.2 (detalle). */
@Service
public class ConsultaDescuentosService {

    private final DescuentoRepository descuentos;
    private final Clock reloj;

    public ConsultaDescuentosService(DescuentoRepository descuentos, Clock reloj) {
        this.descuentos = descuentos;
        this.reloj = reloj;
    }

    /** Solo descuentos activos y vigentes hoy; lista vacía = "no hay descuentos disponibles" (esc. 2 y 3). */
    @Transactional(readOnly = true)
    public List<DescuentoResponse> directorio() {
        LocalDate hoy = LocalDate.now(reloj);
        return descuentos.disponibles(hoy).stream().map(d -> Mapeos.descuento(d, hoy, false)).toList();
    }

    /** Un descuento desactivado o vencido responde 410 (HU 2.2.2 esc. 2). */
    @Transactional(readOnly = true)
    public DescuentoResponse detalle(UUID descuentoId) {
        LocalDate hoy = LocalDate.now(reloj);
        Descuento d = descuentos.buscarConRelaciones(descuentoId).orElseThrow(
                () -> ReglaNegocioException.noEncontrado("DESCUENTO_NO_ENCONTRADO", "El descuento no existe."));
        if (!d.estaDisponible(hoy)) {
            throw ReglaNegocioException.noDisponible("DESCUENTO_NO_DISPONIBLE", "El descuento ya no está disponible.");
        }
        return Mapeos.descuento(d, hoy, false);
    }
}

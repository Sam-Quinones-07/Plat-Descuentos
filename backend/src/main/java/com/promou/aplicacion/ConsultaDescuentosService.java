package com.promou.aplicacion;

import com.promou.aplicacion.dto.DescuentoDtos.DescuentoResponse;
import com.promou.aplicacion.dto.DescuentoDtos.PaginaResponse;
import com.promou.comun.ReglaNegocioException;
import com.promou.comun.Texto;
import com.promou.dominio.descuento.Descuento;
import com.promou.dominio.descuento.DescuentoRepository;
import com.promou.dominio.guardado.GuardadoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU 2.2.1 (directorio), 2.2.2 (detalle) y 2.2.3 (filtro por categoría). */
@Service
public class ConsultaDescuentosService {

    public static final int TAMANO_MAXIMO = 50;

    private final DescuentoRepository descuentos;
    private final GuardadoRepository guardados;
    private final Clock reloj;

    public ConsultaDescuentosService(DescuentoRepository descuentos, GuardadoRepository guardados, Clock reloj) {
        this.descuentos = descuentos;
        this.guardados = guardados;
        this.reloj = reloj;
    }

    /**
     * Solo descuentos activos y vigentes hoy; categoría nula = todas (HU 2.2.3 esc. 3). Lista vacía = "no hay
     * descuentos disponibles" (HU 2.2.1 esc. 2 y 3, HU 2.2.3 esc. 2). Paginado (deuda técnica 8).
     */
    @Transactional(readOnly = true)
    public PaginaResponse<DescuentoResponse> directorio(String categoria, int pagina, int tamano, UUID estudianteId) {
        LocalDate hoy = LocalDate.now(reloj);
        int tam = Math.max(1, Math.min(tamano, TAMANO_MAXIMO));
        Page<Descuento> p = descuentos.disponibles(hoy, Texto.nuloSiVacio(categoria),
                PageRequest.of(Math.max(0, pagina), tam));
        List<UUID> ids = p.getContent().stream().map(Descuento::getId).filter(Objects::nonNull).toList();
        Set<UUID> guardadosIds = estudianteId == null || ids.isEmpty() ? new HashSet<>()
                : new HashSet<>(guardados.idsGuardados(estudianteId, ids));
        List<DescuentoResponse> items = p.getContent().stream()
                .map(d -> Mapeos.descuento(d, hoy, false).conGuardado(guardadosIds.contains(d.getId())))
                .toList();
        return new PaginaResponse<>(items, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    /** Un descuento desactivado o vencido responde 410 (HU 2.2.2 esc. 2, HU 2.2.5 esc. 4). */
    @Transactional(readOnly = true)
    public DescuentoResponse detalle(UUID descuentoId, UUID estudianteId) {
        LocalDate hoy = LocalDate.now(reloj);
        Descuento d = descuentos.buscarConRelaciones(descuentoId).orElseThrow(
                () -> ReglaNegocioException.noEncontrado("DESCUENTO_NO_ENCONTRADO", "El descuento no existe."));
        if (!d.estaDisponible(hoy)) {
            throw ReglaNegocioException.noDisponible("DESCUENTO_NO_DISPONIBLE", "El descuento ya no está disponible.");
        }
        return Mapeos.descuento(d, hoy, false).conGuardado(guardados.existePor(estudianteId, descuentoId));
    }
}

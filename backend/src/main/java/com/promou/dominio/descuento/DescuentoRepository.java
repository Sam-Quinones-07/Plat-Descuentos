package com.promou.dominio.descuento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Pure Fabrication / Indirection: la persistencia de descuentos queda fuera de las entidades del dominio. */
public interface DescuentoRepository extends JpaRepository<Descuento, UUID> {

    String BASE = "select d from Descuento d join fetch d.categoria join fetch d.establecimiento ";

    /**
     * HU 2.2.1 y 2.2.3: descuentos activos y vigentes hoy, opcionalmente de una categoría, paginados (deuda técnica 8).
     * Con categoría nula se listan todas. Se usa countQuery porque el join fetch no sirve para contar.
     */
    @Query(value = BASE + "where d.activo = true and d.fechaInicio <= :hoy and d.fechaFin >= :hoy "
            + "and (:categoria is null or d.categoria.id = :categoria) order by d.fechaFin asc, d.fechaCreacion desc",
            countQuery = "select count(d) from Descuento d where d.activo = true and d.fechaInicio <= :hoy "
                    + "and d.fechaFin >= :hoy and (:categoria is null or d.categoria.id = :categoria)")
    Page<Descuento> disponibles(@Param("hoy") LocalDate hoy, @Param("categoria") String categoria, Pageable pagina);

    /** HU 2.2.2 */
    @Query(BASE + "where d.id = :id")
    Optional<Descuento> buscarConRelaciones(@Param("id") UUID id);

    /** HU 2.1.2 / 2.1.3: solo los descuentos propios; un id ajeno responde como inexistente (control de acceso). */
    @Query(BASE + "where d.id = :id and d.establecimiento.id = :establecimientoId")
    Optional<Descuento> buscarDelEstablecimiento(@Param("id") UUID id,
                                                 @Param("establecimientoId") UUID establecimientoId);

    /** Tablero "Mis descuentos" del establecimiento (HU 2.1.1). */
    @Query(BASE + "where d.establecimiento.id = :establecimientoId order by d.fechaCreacion desc")
    List<Descuento> delEstablecimiento(@Param("establecimientoId") UUID establecimientoId);
}

package com.promou.dominio.descuento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Pure Fabrication / Indirection: la persistencia de descuentos queda fuera de las entidades del dominio. */
public interface DescuentoRepository extends JpaRepository<Descuento, UUID> {

    String BASE = "select d from Descuento d join fetch d.categoria join fetch d.establecimiento ";

    /** HU 2.2.1: solo descuentos activos y vigentes hoy. */
    @Query(BASE + "where d.activo = true and d.fechaInicio <= :hoy and d.fechaFin >= :hoy "
            + "order by d.fechaFin asc, d.fechaCreacion desc")
    List<Descuento> disponibles(@Param("hoy") LocalDate hoy);

    /** HU 2.2.2 */
    @Query(BASE + "where d.id = :id")
    Optional<Descuento> buscarConRelaciones(@Param("id") UUID id);

    /** Tablero "Mis descuentos" del establecimiento (HU 2.1.1). */
    @Query(BASE + "where d.establecimiento.id = :establecimientoId order by d.fechaCreacion desc")
    List<Descuento> delEstablecimiento(@Param("establecimientoId") UUID establecimientoId);
}

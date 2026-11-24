package com.promou.dominio.guardado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuardadoRepository extends JpaRepository<DescuentoGuardado, UUID> {

    @Query("select count(g) > 0 from DescuentoGuardado g where g.estudiante.id = :estudianteId "
            + "and g.descuento.id = :descuentoId")
    boolean existePor(@Param("estudianteId") UUID estudianteId, @Param("descuentoId") UUID descuentoId);

    @Query("select g from DescuentoGuardado g where g.estudiante.id = :estudianteId and g.descuento.id = :descuentoId")
    Optional<DescuentoGuardado> buscar(@Param("estudianteId") UUID estudianteId, @Param("descuentoId") UUID descuentoId);

    /** Ids guardados por el estudiante entre los del directorio (marca "saved" sin una consulta por tarjeta). */
    @Query("select g.descuento.id from DescuentoGuardado g where g.estudiante.id = :estudianteId "
            + "and g.descuento.id in :descuentoIds")
    List<UUID> idsGuardados(@Param("estudianteId") UUID estudianteId, @Param("descuentoIds") List<UUID> descuentoIds);

    @Query("select g from DescuentoGuardado g join fetch g.descuento d join fetch d.categoria join fetch d.establecimiento "
            + "where g.estudiante.id = :estudianteId order by g.guardadoEn desc")
    List<DescuentoGuardado> delEstudiante(@Param("estudianteId") UUID estudianteId);
}

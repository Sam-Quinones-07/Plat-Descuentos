package com.promou.dominio.resena;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResenaRepository extends JpaRepository<Resena, UUID> {

    @Query("select count(r) > 0 from Resena r where r.estudiante.id = :estudianteId and r.descuento.id = :descuentoId")
    boolean existePor(@Param("estudianteId") UUID estudianteId, @Param("descuentoId") UUID descuentoId);

    /** HU 3.1.1: reseñas visibles de un descuento, de la más reciente a la más antigua. */
    @Query("select r from Resena r join fetch r.estudiante where r.descuento.id = :descuentoId and r.retirada = false "
            + "order by r.publicadaEn desc")
    List<Resena> publicadas(@Param("descuentoId") UUID descuentoId);

    /** Moderación del administrador: las más recientes de toda la plataforma. */
    @Query("select r from Resena r join fetch r.estudiante join fetch r.descuento d join fetch d.establecimiento "
            + "order by r.publicadaEn desc")
    List<Resena> recientes(Pageable limite);
}

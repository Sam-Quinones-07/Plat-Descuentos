package com.promou.dominio.usuario;

import com.promou.dominio.afiliacion.EstadoSolicitud;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EstablecimientoRepository extends JpaRepository<Establecimiento, UUID> {

    boolean existsByNitNormalizado(String nitNormalizado);

    Optional<Establecimiento> findByNitNormalizado(String nitNormalizado);

    @Query("select e from Establecimiento e join fetch e.solicitud s where s.estado in :estados order by s.fechaSolicitud desc")
    List<Establecimiento> listarPorEstados(@Param("estados") Collection<EstadoSolicitud> estados);

    @Query("select e from Establecimiento e join fetch e.solicitud s where s.id = :solicitudId")
    Optional<Establecimiento> buscarPorSolicitud(@Param("solicitudId") UUID solicitudId);
}

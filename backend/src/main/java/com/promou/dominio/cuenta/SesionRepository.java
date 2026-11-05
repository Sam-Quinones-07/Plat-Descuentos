package com.promou.dominio.cuenta;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SesionRepository extends JpaRepository<Sesion, UUID> {

    Optional<Sesion> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from Sesion s where s.usuarioId = :usuarioId and s.id <> :conservar")
    int cerrarOtras(@Param("usuarioId") UUID usuarioId, @Param("conservar") UUID conservar);

    @Modifying
    @Query("delete from Sesion s where s.usuarioId = :usuarioId")
    int cerrarTodas(@Param("usuarioId") UUID usuarioId);

    @Modifying
    @Query("delete from Sesion s where s.ultimaActividad < :limite")
    int purgarInactivas(@Param("limite") Instant limite);
}

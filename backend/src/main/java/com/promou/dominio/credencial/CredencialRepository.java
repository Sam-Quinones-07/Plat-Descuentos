package com.promou.dominio.credencial;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CredencialRepository extends JpaRepository<CredencialDigital, UUID> {

    @Query("select c from CredencialDigital c join fetch c.estudiante where c.estudiante.id = :estudianteId and c.revocada = false order by c.fechaEmision desc limit 1")
    Optional<CredencialDigital> ultimaActiva(@Param("estudianteId") UUID estudianteId);

    @Query("select c from CredencialDigital c join fetch c.estudiante where c.codigoQr = :codigo")
    Optional<CredencialDigital> buscarPorCodigo(@Param("codigo") String codigo);
}

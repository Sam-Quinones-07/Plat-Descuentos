package com.promou.dominio.cuenta;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenVerificacionRepository extends JpaRepository<TokenVerificacion, UUID> {

    @Query("select t from TokenVerificacion t join fetch t.usuario where t.tokenHash = :hash and t.tipo = :tipo")
    Optional<TokenVerificacion> buscar(@Param("hash") String hash, @Param("tipo") TokenVerificacion.Tipo tipo);

    /** Al emitir un enlace nuevo, los anteriores del mismo tipo dejan de servir (HU 1.2.2 esc. 5). */
    @Modifying
    @Query("delete from TokenVerificacion t where t.usuario.id = :usuarioId and t.tipo = :tipo")
    int anularAnteriores(@Param("usuarioId") UUID usuarioId, @Param("tipo") TokenVerificacion.Tipo tipo);

    @Modifying
    @Query("delete from TokenVerificacion t where t.expiraEn < :limite")
    int purgarVencidos(@Param("limite") Instant limite);
}

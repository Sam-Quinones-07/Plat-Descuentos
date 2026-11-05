package com.promou.dominio.cuenta;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IntentoLoginRepository extends JpaRepository<IntentoLogin, String> {

    /** Bloqueo de fila: dos intentos simultáneos no pueden saltarse el contador. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from IntentoLogin i where i.claveHash = :clave")
    Optional<IntentoLogin> buscarParaActualizar(@Param("clave") String clave);
}

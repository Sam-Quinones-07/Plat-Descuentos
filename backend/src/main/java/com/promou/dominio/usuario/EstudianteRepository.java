package com.promou.dominio.usuario;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstudianteRepository extends JpaRepository<Estudiante, UUID> {
}

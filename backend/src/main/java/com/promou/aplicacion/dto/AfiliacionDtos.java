package com.promou.aplicacion.dto;

import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class AfiliacionDtos {

    private AfiliacionDtos() {
    }

    public record SolicitudResumen(UUID id, String tradeName, String nit, String representativeName,
                                   Instant submittedAt, String status) {
    }

    public record SolicitudDetalle(UUID id, String tradeName, String legalName, String nit, String businessEmail,
                                   String phone, String address, String representativeName, String document,
                                   String email, boolean emailConfirmed, String status, Instant submittedAt,
                                   Instant reviewedAt, String observations) {
    }

    public record DecisionRequest(@Size(max = 500, message = "Máximo 500 caracteres.") String observations) {
    }
}

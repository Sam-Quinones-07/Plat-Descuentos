package com.promou.aplicacion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Descuentos e identificación digital. Campos del descuento = tipo Discount del front. */
public final class DescuentoDtos {

    private DescuentoDtos() {
    }

    /** Validación de campos y mensajes: la hace {@code Descuento.validar()} (Information Expert). */
    public record DescuentoRequest(String benefit, String conditions, LocalDate startDate, LocalDate endDate,
                                   String code, String location, String website, String category) {
    }

    /** status: active|inactive (como el front); available: activo y dentro de vigencia hoy. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DescuentoResponse(UUID id, String businessName, String benefit, String conditions,
                                    LocalDate startDate, LocalDate endDate, String code, String location,
                                    String website, String category, String status, boolean available) {
    }

    public record CredencialResponse(String digitalId, String firstName, String lastName, String university,
                                     boolean verified, boolean valid, Instant issuedAt, Instant expiresAt) {
    }
}

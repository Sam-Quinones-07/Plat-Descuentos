package com.promou.aplicacion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Descuentos, identificación digital y reseñas. Campos del descuento = tipo Discount del front. */
public final class DescuentoDtos {

    private DescuentoDtos() {
    }

    /** Validación de campos y mensajes: la hace {@code Descuento.validar()} (Information Expert). */
    public record DescuentoRequest(String benefit, String conditions, LocalDate startDate, LocalDate endDate,
                                   String code, String location, String website, String category) {
    }

    /**
     * status: active|inactive; available: activo y dentro de vigencia hoy.
     * code: el dueño siempre lo ve; el estudiante solo si se puede copiar (HU 2.2.5). saved: solo para estudiantes.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DescuentoResponse(UUID id, String businessName, String benefit, String conditions,
                                    LocalDate startDate, LocalDate endDate, String code, String location,
                                    String website, String category, String status, boolean available,
                                    Boolean saved) {

        public DescuentoResponse conGuardado(boolean guardado) {
            return new DescuentoResponse(id, businessName, benefit, conditions, startDate, endDate, code, location,
                    website, category, status, available, guardado);
        }
    }

    /** Página del directorio (deuda técnica 8). page empieza en 0. */
    public record PaginaResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {
    }

    public record CredencialResponse(String digitalId, String firstName, String lastName, String university,
                                     boolean verified, boolean valid, Instant issuedAt, Instant expiresAt) {
    }

    /** HU 1.4.2 */
    public record ValidacionQrRequest(@NotBlank(message = "Ingresa o escanea el código.") @Size(max = 40) String code) {
    }

    /**
     * result: VALIDA | NO_VIGENTE | NO_RECONOCIDA. Responde lo mínimo para validar (Ley 1581): nombre corto,
     * universidad y vencimiento; nunca correo ni documento.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ValidacionQrResponse(String result, String message, String studentName, String university,
                                       Instant expiresAt) {
    }

    /** HU 3.1.2 */
    public record ResenaRequest(@Size(max = 500, message = "La reseña no puede superar 500 caracteres.") String text) {
    }

    /** HU 3.1.1. author es el nombre corto ("Laura G."); mine marca la reseña propia. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ResenaResponse(UUID id, String author, String text, Instant publishedAt, Boolean mine) {
    }

    /** Moderación del administrador. */
    public record ResenaModeracion(UUID id, String author, String businessName, String discountBenefit, String text,
                                   Instant publishedAt, boolean removed) {
    }
}

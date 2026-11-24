package com.promou.aplicacion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Contratos de autenticación y cuenta. Los nombres de campo son los mismos que usa el front (App.tsx). */
public final class AuthDtos {

    public static final String EMAIL = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    public static final String NIT = "^[0-9-]{6,20}$";
    public static final String TELEFONO = "^\\+?[0-9 ()-]{7,20}$";
    public static final String DOCUMENTO = "^[A-Za-z0-9-]{5,24}$";

    private AuthDtos() {
    }

    public record RegistroEstudianteRequest(
            @NotBlank(message = "El nombre es obligatorio.") @Size(max = 80, message = "Máximo 80 caracteres.") String firstName,
            @NotBlank(message = "El apellido es obligatorio.") @Size(max = 80, message = "Máximo 80 caracteres.") String lastName,
            @NotBlank(message = "El correo electrónico es obligatorio.")
            @Size(max = 254, message = "Máximo 254 caracteres.")
            @Pattern(regexp = EMAIL, message = "Ingresa un correo institucional válido.") String email,
            @NotBlank(message = "La universidad es obligatoria.") @Size(max = 120, message = "Máximo 120 caracteres.") String university,
            @NotBlank(message = "La contraseña es obligatoria.") @Size(max = 72, message = "Máximo 72 caracteres.") String password,
            String confirmation,
            @NotNull(message = "Debes aceptar los términos y la política de tratamiento de datos.")
            @AssertTrue(message = "Debes aceptar los términos y la política de tratamiento de datos.") Boolean acceptTerms) {
    }

    public record RegistroEstablecimientoRequest(
            @NotBlank(message = "Este campo es obligatorio.") @Size(max = 120, message = "Máximo 120 caracteres.") String tradeName,
            @NotBlank(message = "Este campo es obligatorio.") @Size(max = 160, message = "Máximo 160 caracteres.") String legalName,
            @NotBlank(message = "Este campo es obligatorio.") @Pattern(regexp = NIT, message = "Ingresa un NIT válido.") String nit,
            @NotBlank(message = "Este campo es obligatorio.") @Size(max = 254, message = "Máximo 254 caracteres.")
            @Pattern(regexp = EMAIL, message = "Ingresa un correo electrónico válido.") String businessEmail,
            @NotBlank(message = "Este campo es obligatorio.") @Pattern(regexp = TELEFONO, message = "Ingresa un número de teléfono válido.") String phone,
            @NotBlank(message = "Este campo es obligatorio.") @Size(max = 200, message = "Máximo 200 caracteres.") String address,
            @NotBlank(message = "Este campo es obligatorio.") @Size(max = 120, message = "Máximo 120 caracteres.") String representativeName,
            @NotBlank(message = "Este campo es obligatorio.") @Pattern(regexp = DOCUMENTO, message = "Ingresa un documento de identidad válido.") String document,
            @NotBlank(message = "Este campo es obligatorio.") @Size(max = 254, message = "Máximo 254 caracteres.")
            @Pattern(regexp = EMAIL, message = "Ingresa un correo electrónico válido.") String email,
            @NotBlank(message = "Este campo es obligatorio.") @Size(max = 72, message = "Máximo 72 caracteres.") String password,
            String confirmation,
            @NotNull(message = "Debes aceptar los términos y la política de tratamiento de datos.")
            @AssertTrue(message = "Debes aceptar los términos y la política de tratamiento de datos.") Boolean acceptTerms) {
    }

    public record LoginRequest(
            @NotBlank(message = "El correo electrónico es obligatorio.") String email,
            @NotBlank(message = "La contraseña es obligatoria.") String password) {
    }

    public record MensajeResponse(String message) {
    }

    /** Campos nulos se omiten: un estudiante no trae tradeName, un establecimiento no trae university. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PerfilDto(String role, String email, String firstName, String lastName, String university,
                            String tradeName, String status) {
    }

    /** El token no va en el cuerpo: viaja en la cookie HttpOnly (deuda técnica 4). */
    public record LoginResponse(long inactivityTimeoutSeconds, PerfilDto user) {
    }

    /** HU 1.3.1 */
    public record CambioContrasenaRequest(
            @NotBlank(message = "Ingresa tu contraseña actual.") String currentPassword,
            @NotBlank(message = "Ingresa la nueva contraseña.") @Size(max = 72, message = "Máximo 72 caracteres.") String newPassword,
            String confirmation) {
    }

    /** HU 1.2.2 esc. 5 (reenviar confirmación) y HU 1.3.2 esc. 1 (solicitar restablecimiento). */
    public record CorreoRequest(
            @NotBlank(message = "El correo electrónico es obligatorio.")
            @Size(max = 254, message = "Máximo 254 caracteres.")
            @Pattern(regexp = EMAIL, message = "Ingresa un correo electrónico válido.") String email) {
    }

    /** HU 1.3.2 esc. 3 */
    public record RestablecerRequest(
            @NotBlank(message = "El enlace no es válido.") String token,
            @NotBlank(message = "Ingresa la nueva contraseña.") @Size(max = 72, message = "Máximo 72 caracteres.") String password,
            String confirmation) {
    }

    public record ConfirmacionRequest(@NotBlank(message = "El enlace no es válido.") String token) {
    }

    /** role indica a qué inicio de sesión llevar al usuario después de confirmar. */
    public record ConfirmacionResponse(String message, String role) {
    }
}

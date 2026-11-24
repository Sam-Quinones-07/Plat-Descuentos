package com.promou.aplicacion.notificacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adaptador (deuda técnica 2): traduce {@link NotificacionService} a la API HTTPS transaccional de Brevo.
 * Render gratis bloquea los puertos SMTP, pero no el tráfico HTTPS, por eso este adaptador sí funciona allí.
 * Los servicios no cambian: solo se elige otro adaptador en {@code NotificacionConfig}.
 */
public class CorreoBrevoAdapter implements NotificacionService {

    private static final URI API = URI.create("https://api.brevo.com/v3/smtp/email");
    private static final Pattern REMITENTE = Pattern.compile("^\\s*(.*?)\\s*<([^>]+)>\\s*$");

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper json;
    private final String apiKey;
    private final String nombreRemitente;
    private final String correoRemitente;

    public CorreoBrevoAdapter(ObjectMapper json, String apiKey, String remitente) {
        this.json = json;
        this.apiKey = apiKey;
        Matcher m = REMITENTE.matcher(remitente);
        this.nombreRemitente = m.matches() ? m.group(1) : "PromoU";
        this.correoRemitente = m.matches() ? m.group(2) : remitente.trim();
    }

    @Override
    public void enviar(Mensaje mensaje) {
        Map<String, Object> cuerpo = Map.of(
                "sender", Map.of("name", nombreRemitente, "email", correoRemitente),
                "to", List.of(Map.of("email", mensaje.destinatario())),
                "subject", mensaje.asunto(),
                "textContent", mensaje.cuerpo());
        try {
            HttpRequest peticion = HttpRequest.newBuilder(API)
                    .timeout(Duration.ofSeconds(15))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(cuerpo)))
                    .build();
            HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() >= 300) {
                throw new IllegalStateException("Brevo respondió " + respuesta.statusCode() + ": " + respuesta.body());
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo contactar a Brevo: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Envío interrumpido", e);
        }
    }
}

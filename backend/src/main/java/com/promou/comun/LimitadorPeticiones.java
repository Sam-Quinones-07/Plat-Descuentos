package com.promou.comun;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite de peticiones por clave en una ventana fija (deuda técnica 6): por IP en /api/auth/** y por correo en el
 * envío de enlaces. Vive en memoria: basta para una sola instancia del backend; con varias habría que pasarlo a la
 * base o a Redis.
 */
public class LimitadorPeticiones {

    private record Ventana(Instant inicio, int cuenta) {
    }

    private final int maximo;
    private final Duration duracion;
    private final Map<String, Ventana> ventanas = new ConcurrentHashMap<>();

    public LimitadorPeticiones(int maximo, Duration duracion) {
        this.maximo = maximo;
        this.duracion = duracion;
    }

    /** @return true si la petición se permite (y la cuenta); false si la clave superó el máximo en la ventana. */
    public boolean permitir(String clave, Instant ahora) {
        Ventana v = ventanas.compute(clave, (k, actual) ->
                actual == null || !ahora.isBefore(actual.inicio().plus(duracion))
                        ? new Ventana(ahora, 1)
                        : new Ventana(actual.inicio(), actual.cuenta() + 1));
        if (ventanas.size() > 10_000) {
            purgar(ahora);
        }
        return v.cuenta() <= maximo;
    }

    private void purgar(Instant ahora) {
        ventanas.entrySet().removeIf(e -> !ahora.isBefore(e.getValue().inicio().plus(duracion)));
    }
}

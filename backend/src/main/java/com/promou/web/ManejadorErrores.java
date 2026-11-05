package com.promou.web;

import com.promou.comun.ReglaNegocioException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ReglaNegocioException.class)
    ResponseEntity<ErrorApi> reglaNegocio(ReglaNegocioException ex) {
        return ResponseEntity.status(ex.getTipo().http())
                .body(ErrorApi.de(ex.getCodigo(), ex.getMessage(), ex.getCampos()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ErrorApi.de("DATOS_INVALIDOS", "Revisa los campos del formulario.", campos));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(ErrorApi.de("CUERPO_INVALIDO",
                "La solicitud no tiene un formato válido (revisa los tipos y las fechas AAAA-MM-DD).", null));
    }

    /** Dos administradores resolviendo la misma solicitud a la vez, etc. */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ErrorApi> concurrencia(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorApi.de("CONFLICTO_CONCURRENCIA",
                "El recurso cambió mientras lo editabas. Recárgalo e inténtalo de nuevo.", null));
    }

    /** Última defensa ante carreras sobre restricciones únicas (correo, guardado, reseña). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorApi> integridad(DataIntegrityViolationException ex) {
        LOG.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorApi.de("CONFLICTO", "No fue posible completar la operación.", null));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorApi> inesperado(Exception ex) {
        LOG.error("Error inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorApi.de("ERROR_INTERNO", "Ocurrió un error inesperado. Inténtalo más tarde.", null));
    }
}

package com.edu.sistema_inventario.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = baseProblem(
                HttpStatus.BAD_REQUEST,
                "https://api.stockflow.com/errors/validation-failed",
                "Error de validacion en los datos",
                "Los parametros proporcionados no cumplen con las reglas requeridas."
        );
        problemDetail.setProperty("invalidParams", collectFieldErrors(ex));
        return problemDetail;
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        return baseProblem(
                ex.getStatus(),
                "https://api.stockflow.com/errors/" + ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return baseProblem(
                HttpStatus.BAD_REQUEST,
                "https://api.stockflow.com/errors/invalid-request",
                "Solicitud invalida",
                ex.getMessage()
        );
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ProblemDetail handleDateTimeParse(DateTimeParseException ex) {
        return baseProblem(
                HttpStatus.BAD_REQUEST,
                "https://api.stockflow.com/errors/invalid-date-format",
                "Formato de fecha invalido",
                "Formato de fecha invalido, use ISO_LOCAL_DATE_TIME. " + ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = String.format("El parametro '%s' tiene un valor invalido: %s", ex.getName(), ex.getValue());
        return baseProblem(
                HttpStatus.BAD_REQUEST,
                "https://api.stockflow.com/errors/type-mismatch",
                "Tipo de parametro invalido",
                detail
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return baseProblem(
                HttpStatus.BAD_REQUEST,
                "https://api.stockflow.com/errors/message-not-readable",
                "JSON invalido o formato no legible",
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return baseProblem(
                HttpStatus.FORBIDDEN,
                "https://api.stockflow.com/errors/access-denied",
                "Acceso denegado",
                ex.getMessage()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return baseProblem(
                HttpStatus.UNAUTHORIZED,
                "https://api.stockflow.com/errors/authentication-failed",
                "Autenticacion fallida",
                ex.getMessage()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return baseProblem(
                HttpStatus.METHOD_NOT_ALLOWED,
                "https://api.stockflow.com/errors/method-not-allowed",
                "Metodo HTTP no permitido",
                ex.getMessage()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        return baseProblem(
                HttpStatus.NOT_FOUND,
                "https://api.stockflow.com/errors/entity-not-found",
                "Recurso no encontrado",
                ex.getMessage()
        );
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ProblemDetail handleEmptyResult(EmptyResultDataAccessException ex) {
        return baseProblem(
                HttpStatus.NOT_FOUND,
                "https://api.stockflow.com/errors/not-found",
                "Recurso no encontrado",
                ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAll(Exception ex) {
        return baseProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "https://api.stockflow.com/errors/internal-server-error",
                "Error interno del servidor",
                ex.getMessage()
        );
    }

    private Map<String, String> collectFieldErrors(MethodArgumentNotValidException ex) {
        Map<String, String> invalidParams = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> invalidParams.put(error.getField(), error.getDefaultMessage()));
        return invalidParams;
    }

    private ProblemDetail baseProblem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setType(URI.create(type));
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setProperty("message", detail);
        return problemDetail;
    }
}

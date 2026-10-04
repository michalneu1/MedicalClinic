package com.medicalclinic.exception;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MedicalClinicException.class)
    public ProblemDetail handleMedicalClinic(MedicalClinicException exception) {
        log.warn("Domain error: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        List<Map<String, String>> errors = collectFieldErrors(exception);
        log.warn("Validation failed: {}", errors.size());
        return buildValidationProblem(errors);
    }

    private List<Map<String, String>> collectFieldErrors(MethodArgumentNotValidException exception) {
        return exception.getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(this::toErrorEntry)
                .toList();
    }

    private Map<String, String> toErrorEntry(FieldError error) {
        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("field", error.getField());
        entry.put("message", String.valueOf(error.getDefaultMessage()));
        return entry;
    }

    private ProblemDetail buildValidationProblem(List<Map<String, String>> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException exception) {
        log.warn("Unreadable body: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is not valid JSON");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoRoute(NoResourceFoundException exception) {
        log.warn("No route: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "No such endpoint");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMismatch(MethodArgumentTypeMismatchException exception) {
        log.warn("Type mismatch: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Parameter " + exception.getName() + " has invalid type");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknownError(Exception exception) {
        log.error("Unexpected error", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error");
    }
}

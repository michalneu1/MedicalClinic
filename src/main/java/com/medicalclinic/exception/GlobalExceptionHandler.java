package com.medicalclinic.exception;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleObjectOptimisticLocking(ObjectOptimisticLockingFailureException exception) {
        log.warn("Version conflict: {}", exception.getPersistentClassName());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The resource was changed by another request. Fetch it again and retry.");
    }

    @ExceptionHandler(MedicalClinicException.class)
    public ProblemDetail handleMedicalClinic(MedicalClinicException exception) {
        log.warn("Domain error: {} {}", exception.getStatus().value(), exception.getClass().getSimpleName());
        return ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<Map<String, String>> errors = collectFieldErrors(exception);
        log.warn("Validation failed: {}", errors.size());
        return ResponseEntity.status(status).headers(headers).body(buildValidationProblem(errors));
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

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return respond(exception, "request body is not valid JSON", headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return respond(exception, "method " + exception.getMethod() + " is not supported here",
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException exception,
                                                                    HttpHeaders headers,
                                                                    HttpStatusCode status,
                                                                    WebRequest request) {
        return respond(exception, "No such endpoint", headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException exception,
                                                        HttpHeaders headers,
                                                        HttpStatusCode status,
                                                        WebRequest request) {
        String detail = exception instanceof MethodArgumentTypeMismatchException mismatch
                ? "Parameter " + mismatch.getName() + " has invalid type"
                : "Parameter has invalid type";
        return respond(exception, detail, headers, status, request);
    }

    private ResponseEntity<Object> respond(Exception exception,
                                           String detail,
                                           HttpHeaders headers,
                                           HttpStatusCode status,
                                           WebRequest request) {
        return handleExceptionInternal(exception, ProblemDetail.forStatusAndDetail(status, detail),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode status,
                                                             WebRequest request) {
        log.warn("Request rejected: {} {}", status.value(), exception.getClass().getSimpleName());
        return super.handleExceptionInternal(exception, body, headers, status, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException exception) {
        log.warn("Authentication failed: {}", exception.getClass().getSimpleName());
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "wrong e-mail or password");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknownError(Exception exception) {
        log.error("Unexpected error", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error");
    }
}

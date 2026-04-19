package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.services.exceptions.CommitException;
import com.ksig.tu_vcs.services.exceptions.ResourceAlreadyExistsException;
import com.ksig.tu_vcs.services.exceptions.ResourceNotFoundException;
import com.ksig.tu_vcs.services.views.ErrorView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    private ErrorView buildErrorView(String message, HttpServletRequest request) {
        ErrorView errorView = new ErrorView();
        String logId = (String) request.getAttribute("logId");
        errorView.setLogId(logId);
        errorView.setMessage(message);
        errorView.setTimestamp(LocalDateTime.now());

        return errorView;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorView> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorView view = buildErrorView("Validation failed: " + errorMessage, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(view);
    }


    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorView> handleBadArguments(
            RuntimeException ex,
            HttpServletRequest request) {

        ErrorView view = buildErrorView(ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(view);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorView> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ErrorView view = buildErrorView(ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(view);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorView> handleAlreadyExists(ResourceAlreadyExistsException ex, HttpServletRequest request) {
        ErrorView view = buildErrorView(ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(view);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorView> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        ErrorView view = buildErrorView("Access denied: " + ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(view);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorView> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        ErrorView view = buildErrorView("Data integrity error: " + ex.getMostSpecificCause().getMessage(), request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(view);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ErrorView> handleDatabaseDown(
            DataAccessResourceFailureException ex, HttpServletRequest request) {

        ErrorView view = buildErrorView("The database is currently unavailable. Please try again later.", request);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(view);
    }

    @ExceptionHandler(CommitException.class)
    public ResponseEntity<ErrorView> handleCommitException(CommitException ex, HttpServletRequest request) {
        ErrorView view = buildErrorView("Error during commit! " + ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(view);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorView> handleAllOtherExceptions(
            Exception ex,
            HttpServletRequest request) {
        ErrorView view = buildErrorView("An unexpected error occurred. " + ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(view);
    }
}
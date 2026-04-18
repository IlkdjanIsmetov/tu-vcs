package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.services.exceptions.CommitException;
import com.ksig.tu_vcs.services.exceptions.ResourceAlreadyExistsException;
import com.ksig.tu_vcs.services.exceptions.ResourceNotFoundException;
import com.ksig.tu_vcs.services.views.*;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @Test
    void shouldHandleValidationErrors() {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("logId")).thenReturn("log123");

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError error1 = new FieldError("obj", "field1", "must not be null");
        FieldError error2 = new FieldError("obj", "field2", "invalid");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorView> response =
                handler.handleValidationErrors(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        String message = response.getBody().getMessage();
        assertTrue(message.contains("field1"));
        assertTrue(message.contains("field2"));
        assertTrue(message.contains("Validation failed"));

        assertEquals("log123", response.getBody().getLogId());
    }

    @Test
    void shouldHandleIllegalArgumentException() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("logId")).thenReturn("log123");

        IllegalArgumentException ex = new IllegalArgumentException("bad arg");

        ResponseEntity<ErrorView> response =
                handler.handleBadArguments(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad arg", response.getBody().getMessage());
        assertEquals("log123", response.getBody().getLogId());
    }

    @Test
    void shouldHandleIllegalStateException() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("logId")).thenReturn("log456");

        IllegalStateException ex = new IllegalStateException("bad state");

        ResponseEntity<ErrorView> response =
                handler.handleBadArguments(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad state", response.getBody().getMessage());
        assertEquals("log456", response.getBody().getLogId());
    }

    @Test
    void shouldHandleResourceNotFoundException() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("logId")).thenReturn("log789");

        ResourceNotFoundException ex =
                new ResourceNotFoundException("not found");

        ResponseEntity<ErrorView> response =
                handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("not found", response.getBody().getMessage());
        assertEquals("log789", response.getBody().getLogId());
    }

    @Test
    void shouldHandleResourceAlreadyExistsException() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("logId")).thenReturn("log999");

        ResourceAlreadyExistsException ex =
                new ResourceAlreadyExistsException("already exists");

        ResponseEntity<ErrorView> response =
                handler.handleAlreadyExists(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("already exists", response.getBody().getMessage());
        assertEquals("log999", response.getBody().getLogId());
    }

    @Test
    void shouldHandleDatabaseDown() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("logId")).thenReturn("log-db");

        DataAccessResourceFailureException ex =
                new DataAccessResourceFailureException("db down");

        ResponseEntity<ErrorView> response =
                handler.handleDatabaseDown(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("The database is currently unavailable. Please try again later.",
                response.getBody().getMessage());
        assertEquals("log-db", response.getBody().getLogId());
    }

    @Test
    void shouldHandleCommitException() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("logId")).thenReturn("log-commit");

        CommitException ex = new CommitException("failure");

        ResponseEntity<ErrorView> response =
                handler.handleCommitException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Error during commit! failure",
                response.getBody().getMessage());
        assertEquals("log-commit", response.getBody().getLogId());
    }

    @Test
    void shouldHandleGenericException() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("logId")).thenReturn("log-generic");

        Exception ex = new Exception("something went wrong");

        ResponseEntity<ErrorView> response =
                handler.handleAllOtherExceptions(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred. something went wrong",
                response.getBody().getMessage());
        assertEquals("log-generic", response.getBody().getLogId());
    }
}
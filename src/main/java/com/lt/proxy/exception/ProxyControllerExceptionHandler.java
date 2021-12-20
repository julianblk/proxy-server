package com.lt.proxy.exception;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ProxyControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyControllerExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        LOGGER.error("Exception handled", ex);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                Map.of(
                    "errorMessage", ex.getMessage()
                )
            );
    }

    @ExceptionHandler(ProxyException.class)
    public ResponseEntity<Map<String, String>> handleProxyException(ProxyException ex, HttpServletRequest request) {
        LOGGER.error("Exception handled", ex);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                Map.of(
                    "errorMessage", ex.getMessage()
                )
            );
    }
}

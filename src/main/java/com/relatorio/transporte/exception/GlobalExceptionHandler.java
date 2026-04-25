package com.relatorio.transporte.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> business(BusinessException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> illegalArgument(IllegalArgumentException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> notReadable(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "JSON invalido: " + ex.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("Validacao falhou");
        return resposta(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generic(Exception ex) {
        log.error("Erro nao tratado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno: " + ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", mensagem == null ? "" : mensagem
        ));
    }
}

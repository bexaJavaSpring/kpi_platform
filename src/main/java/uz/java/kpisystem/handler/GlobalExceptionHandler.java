package uz.java.kpisystem.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import uz.java.kpisystem.exception.*;
import uz.java.kpisystem.util.ErrorUtil;
import uz.java.kpisystem.util.Translator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice // gloabal chiqqan exception handle qiladi(faqat @Valid dan tashqari)
@Slf4j
public class GlobalExceptionHandler {
    private final Translator translator;

    public GlobalExceptionHandler(Translator translator) {
        this.translator = translator;
    }

    @ExceptionHandler(CustomNotFoundException.class)
    public ResponseEntity<Object> handleCustomNotFoundException(CustomNotFoundException ex) {
        log.error("GenericNotFoundException on: {}", ErrorUtil.getStacktrace(ex));
        return new ResponseEntity<>(Map.of("message", translator.toLocale(ex.getMessage())),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RedisNotSerializableException.class)
    public ResponseEntity<Object> handleRedisNotSerializableException(RedisNotSerializableException ex) {
        log.error("RedisNotSerializableException on: {}", ErrorUtil.getStacktrace(ex));
        return new ResponseEntity<>(Map.of("message", translator.toLocale(ex.getMessage())),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidError(final MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException on: {}", ErrorUtil.getStacktrace(ex));
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError ->
                {
                    if (!translator.toLocale(fieldError.getDefaultMessage()).equals(fieldError.getDefaultMessage())) {
                        return Objects.requireNonNull(translator.toLocale(fieldError.getDefaultMessage()));
                    } else {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }

                }).toList();
        return new ResponseEntity<>(Map.of("message", errors.get(0)), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GenericRuntimeException.class)
    public ResponseEntity<Object> handleGenericRuntimeException(GenericRuntimeException ex) {
        return new ResponseEntity<>(Map.of("message", List.of(translator.toLocale(ex.getMessage()))),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNotFound(NoHandlerFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", translator.toLocale("api.not.found"));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }


    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Object> handleFileNotFoundException(FileNotFoundException ex) {
        log.error("FileNotFoundException on: {}", ErrorUtil.getStacktrace(ex));
        return new ResponseEntity<>(Map.of("message", translator.toLocale(ex.getMessage())),
                HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Object> handleFileStorageException(FileStorageException ex) {
        log.error("FileStorageException on: {}", ErrorUtil.getStacktrace(ex));
        return new ResponseEntity<>(Map.of("message", translator.toLocale(ex.getMessage())),
                HttpStatus.NOT_FOUND);
    }


}

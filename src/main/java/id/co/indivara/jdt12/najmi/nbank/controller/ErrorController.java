package id.co.indivara.jdt12.najmi.nbank.controller;

import com.fasterxml.jackson.core.JsonParseException;
import id.co.indivara.jdt12.najmi.nbank.model.response.OccuredError;
import id.co.indivara.jdt12.najmi.nbank.model.response.WebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.lang.model.type.NullType;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;


@RestControllerAdvice
@Slf4j
public class ErrorController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<WebResponse<NullType, ArrayList<String>>> constraintViolation(ConstraintViolationException e){

        ArrayList<String> err = new ArrayList<>();

        for(ConstraintViolation<?> r: e.getConstraintViolations()){
            err.add(r.getPropertyPath().toString() + " " + r.getMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                WebResponse.<NullType, ArrayList<String>>builder()
                        .message("Request Failed")
                        .timestamp(LocalDateTime.now())
                        .data(null)
                        .error(
                                OccuredError.<ArrayList<String>>builder()
                                        .name("Constraints Violations")
                                        .detail(err)
                                        .build()
                        )
                        .build()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<WebResponse<String, String>> responseResponseStatusException(ResponseStatusException e){
        String[] reasons = Objects.requireNonNull(e.getReason()).split(" : ");

        return ResponseEntity.status(e.getRawStatusCode()).body(

                WebResponse.<String, String>builder()
                        .message("Request Failed")
                        .timestamp(LocalDateTime.now())
                        .data(null)
                        .error(
                                OccuredError.<String>builder()
                                        .name(reasons[0])
                                        .detail(reasons[1])
                                        .build()
                        )
                        .build()
        );
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ex);
        return ResponseEntity.status(status).body(
                WebResponse.<NullType, Object>builder()
                        .message("Request Failed")
                        .timestamp(LocalDateTime.now())
                        .data(null)
                        .error(
                                OccuredError.builder()
                                        .name(status.getReasonPhrase())
                                        .detail(ex.getMessage())
                                        .build()
                        )
                        .build()
        );
    }
}

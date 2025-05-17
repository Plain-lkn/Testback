package org.example.plain.common.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.plain.common.ResponseField;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;


@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({SQLException.class})
    public ResponseEntity<ResponseField> handleException(Exception e) {
        ResponseField responseField = new ResponseField(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        return new ResponseEntity<>(responseField, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity<ResponseField> handleNullPointerException(NullPointerException e) {
        ResponseField responseField = new ResponseField<>(e.getMessage(), HttpStatus.NOT_FOUND,null);
        return new ResponseEntity<>(responseField, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ResponseField> hanadleAuthenticationFailed(AuthenticationException e){
        ResponseField responseField = new ResponseField(e.getMessage(), HttpStatus.UNAUTHORIZED,null);
        return new ResponseEntity<>(responseField, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({HttpClientErrorException.class})
    public ResponseEntity<ResponseField> handleHttpClientErrorException(HttpClientErrorException e){
        ResponseField responseField = new ResponseField(e.getMessage(), (HttpStatus) e.getStatusCode(),null);
        return new ResponseEntity<>(responseField, e.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseField> handlerOtherException(Exception e) {
        ResponseField responseField = new ResponseField(e.getMessage(), HttpStatus.valueOf(400),null);
        log.error("",e.fillInStackTrace());
        return new ResponseEntity<>(responseField, HttpStatus.valueOf(400));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ResponseField responseField = new ResponseField(ex.getMessage(), (HttpStatus) statusCode, body);
        return new ResponseEntity<>(responseField, statusCode);
    }
}

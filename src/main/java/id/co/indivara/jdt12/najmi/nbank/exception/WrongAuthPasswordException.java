package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class WrongAuthPasswordException extends ResponseStatusException {
    public WrongAuthPasswordException(){
        super(HttpStatus.BAD_REQUEST, "Authentication Failed : Wrong Password");
    }
}

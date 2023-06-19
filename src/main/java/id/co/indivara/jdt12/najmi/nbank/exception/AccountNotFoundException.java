package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AccountNotFoundException extends ResponseStatusException {
    public AccountNotFoundException(){
        super(HttpStatus.BAD_REQUEST, "Account Not Found : Account you are refering to could not be found");
    }
}

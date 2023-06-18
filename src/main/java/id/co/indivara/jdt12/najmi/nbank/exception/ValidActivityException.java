package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ValidActivityException extends ResponseStatusException {
    public ValidActivityException(){
        super(HttpStatus.BAD_REQUEST, "Transaction Activity Not Valid : Only Accept transfer, deposit, withdraw, all");
    }
}

package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InsufficientBalanceException extends ResponseStatusException {
    public InsufficientBalanceException(){
        super(HttpStatus.BAD_REQUEST, "Balance Error : Insufficient Balance To Complete This Transaction");
    }
}

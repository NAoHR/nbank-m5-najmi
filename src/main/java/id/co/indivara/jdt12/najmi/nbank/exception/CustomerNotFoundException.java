package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CustomerNotFoundException extends ResponseStatusException {
    public CustomerNotFoundException(){
        super(HttpStatus.BAD_REQUEST, "Customer Not Found : Customer you are refering to could not be found");
    }
}

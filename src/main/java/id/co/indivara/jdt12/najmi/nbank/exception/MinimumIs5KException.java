package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class MinimumIs5KException extends ResponseStatusException {
    public MinimumIs5KException(){
        super(HttpStatus.BAD_REQUEST, "Minimum Amount Violation : Minimum To Do This Transaction Is Rp 5.000");
    }
}

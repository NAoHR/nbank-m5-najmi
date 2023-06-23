package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MultipleOf50kException extends ResponseStatusException {
    public MultipleOf50kException(){
        super(HttpStatus.BAD_REQUEST, "Money Error : Money Needs to Be a Multiple Of Rp 50.000");
    }
}
package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SessionExpiredException extends ResponseStatusException {
    public SessionExpiredException(){
        super(HttpStatus.UNAUTHORIZED, "Session Expired : Please Login Again");
    }
}

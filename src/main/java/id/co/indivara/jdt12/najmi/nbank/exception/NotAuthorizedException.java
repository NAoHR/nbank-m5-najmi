package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotAuthorizedException extends ResponseStatusException  {
    public NotAuthorizedException(){
        super(HttpStatus.UNAUTHORIZED, "Unauthorized : Make Sure You Are Logged In First");
    }
}

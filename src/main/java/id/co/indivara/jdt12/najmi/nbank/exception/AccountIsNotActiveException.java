package id.co.indivara.jdt12.najmi.nbank.exception;

import id.co.indivara.jdt12.najmi.nbank.enums.StatusEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AccountIsNotActiveException extends ResponseStatusException {
    public AccountIsNotActiveException(){
        super(HttpStatus.BAD_REQUEST, "Account Is Not Active : Please Contact Your Admin To Reactivate Your Account");
    }

    public AccountIsNotActiveException(String s){
        super(HttpStatus.BAD_REQUEST, String.format("Account Is Not Active : %s", s));
    }
}

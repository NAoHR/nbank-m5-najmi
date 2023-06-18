package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ItsTimeDepositAccountException extends ResponseStatusException {
    public ItsTimeDepositAccountException(String name){
        super(HttpStatus.BAD_REQUEST, String.format("Transaction Error : Cannot Do %s, Because It's a Time Deposit Account",name));
    }
}

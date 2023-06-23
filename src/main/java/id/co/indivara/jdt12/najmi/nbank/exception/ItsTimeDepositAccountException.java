package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;

public class ItsTimeDepositAccountException extends ResponseStatusException {
    public ItsTimeDepositAccountException(String name, String time){
        super(HttpStatus.BAD_REQUEST, String.format("Time Deposit Violation : Cannot Do %s, Because It's a Time Deposit Account; you need to wait until %s",name, time));
    }
}

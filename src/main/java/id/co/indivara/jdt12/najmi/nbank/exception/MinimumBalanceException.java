package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

public class MinimumBalanceException extends ResponseStatusException {
    public MinimumBalanceException(String name, BigDecimal money){
        super(HttpStatus.BAD_REQUEST, String.format("Minimum Balance Error : Could not do %s Transaction, Because Balance Must Not Below %.3f", name, money));
    }
}

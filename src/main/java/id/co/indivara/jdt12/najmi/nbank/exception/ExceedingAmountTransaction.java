package id.co.indivara.jdt12.najmi.nbank.exception;

import id.co.indivara.jdt12.najmi.nbank.enums.TransactionTypeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ExceedingAmountTransaction extends ResponseStatusException {
    public ExceedingAmountTransaction(TransactionTypeEnum t){
        super(HttpStatus.BAD_REQUEST, String.format("Exceeding Amount : You have Passed Daily Maximum %s Transaction", t));
    }
}

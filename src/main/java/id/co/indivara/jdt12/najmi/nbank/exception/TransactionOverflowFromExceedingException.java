package id.co.indivara.jdt12.najmi.nbank.exception;

import id.co.indivara.jdt12.najmi.nbank.enums.TransactionTypeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

public class TransactionOverflowFromExceedingException  extends ResponseStatusException {
    public TransactionOverflowFromExceedingException(TransactionTypeEnum t, BigDecimal money){
        super(HttpStatus.BAD_REQUEST, String.format("Overflown Transaction : Your %s Transaction Only Left %.3f for today", t, money));
    }
}

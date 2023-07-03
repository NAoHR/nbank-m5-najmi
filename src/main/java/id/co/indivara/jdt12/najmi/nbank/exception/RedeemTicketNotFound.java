package id.co.indivara.jdt12.najmi.nbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RedeemTicketNotFound extends ResponseStatusException {
    public RedeemTicketNotFound(){
        super(HttpStatus.BAD_REQUEST, "Ticket Not found : Ticket You Specified Could Not Be Found");
    }
}

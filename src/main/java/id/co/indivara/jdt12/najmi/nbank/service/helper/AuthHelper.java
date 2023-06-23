package id.co.indivara.jdt12.najmi.nbank.service.helper;

import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.exception.WrongAuthPasswordException;
import id.co.indivara.jdt12.najmi.nbank.security.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {
    public final void validatePassword(String password, String hashedPassword){
        if(!BCrypt.checkpw(password, hashedPassword)){
            throw new WrongAuthPasswordException();
        }
    }
}

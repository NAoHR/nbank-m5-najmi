package id.co.indivara.jdt12.najmi.nbank.service.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Set;

@Service
public class ValidatorService {

    @Autowired
    Validator validator;
    public void validate(Object e){
        Set<ConstraintViolation<Object>> violations = validator.validate(e);
        if(violations.size() != 0){
            throw new ConstraintViolationException(violations);
        }
    }
}

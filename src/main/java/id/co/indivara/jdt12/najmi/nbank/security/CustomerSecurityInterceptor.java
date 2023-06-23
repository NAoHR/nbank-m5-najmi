package id.co.indivara.jdt12.najmi.nbank.security;

import id.co.indivara.jdt12.najmi.nbank.entity.Customer;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountOrCustomerEnum;
import id.co.indivara.jdt12.najmi.nbank.exception.CustomerNotFoundException;
import id.co.indivara.jdt12.najmi.nbank.repo.CustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class CustomerSecurityInterceptor implements HandlerInterceptor {
    @Autowired
    private JsonWebToken jsonWebToken;


    @Autowired
    private CustomerRepo customerRepo;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UUID userid = jsonWebToken.parseToken(request.getHeader("Authorization"), AccountOrCustomerEnum.CUSTOMER);
        Customer customer = customerRepo.findById(userid).orElseThrow(CustomerNotFoundException::new);


        request.setAttribute("customer", customer);
        return true;
    }
}

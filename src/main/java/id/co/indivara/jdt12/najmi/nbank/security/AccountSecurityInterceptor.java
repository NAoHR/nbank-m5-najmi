package id.co.indivara.jdt12.najmi.nbank.security;

import id.co.indivara.jdt12.najmi.nbank.entity.Account;
import id.co.indivara.jdt12.najmi.nbank.enums.AccountOrCustomerEnum;
import id.co.indivara.jdt12.najmi.nbank.exception.AccountNotFoundException;
import id.co.indivara.jdt12.najmi.nbank.repo.AccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class AccountSecurityInterceptor implements HandlerInterceptor {
    @Autowired
    private JsonWebToken jsonWebToken;

    @Autowired
    private AccountRepo accountRepo;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UUID userid = jsonWebToken.parseToken(request.getHeader("Authorization"), AccountOrCustomerEnum.ACCOUNT);

        Account account = accountRepo.findById(userid).orElseThrow(AccountNotFoundException::new);
        request.setAttribute("account", account);

        return true;
    }
}

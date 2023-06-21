package id.co.indivara.jdt12.najmi.nbank;

import id.co.indivara.jdt12.najmi.nbank.security.AccountSecurityInterceptor;
import id.co.indivara.jdt12.najmi.nbank.security.AdminSecurity;
import id.co.indivara.jdt12.najmi.nbank.security.CustomerSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    CustomerSecurityInterceptor customerSecurityInterceptor;

    @Autowired
    AccountSecurityInterceptor accountSecurityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(customerSecurityInterceptor)
                .addPathPatterns("/api/auth/customer/logout")
        ;

        registry.addInterceptor(accountSecurityInterceptor)
                .addPathPatterns("/api/auth/account/logout")
        ;
    }
}
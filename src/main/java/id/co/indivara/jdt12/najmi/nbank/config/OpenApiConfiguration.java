package id.co.indivara.jdt12.najmi.nbank.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "nbank api swagger",
                description = "nbank api documentation",
                contact = @Contact(
                        name = "Najmi",
                        email = "najmim625@gmail.com",
                        url = "https://naohr.vercel.app"
                )
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "PROD ENV",
                        url = "https://naohr.vercel.app"
                )
        }
)


@SecuritySchemes({
        @SecurityScheme(
                name = "basicAuth",
                description = "admin related security",
                type = SecuritySchemeType.HTTP,
                scheme = "basic"
        ),
        @SecurityScheme(
                name = "bearerCustomerAuth",
                description = "customer related security",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                in = SecuritySchemeIn.HEADER
        ),
        @SecurityScheme(
                name = "bearerAccountAuth",
                description = "account related security",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                in = SecuritySchemeIn.HEADER
        )
})
public class OpenApiConfiguration {
}

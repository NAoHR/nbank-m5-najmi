import id.co.indivara.jdt12.najmi.nbank.controller.AdminControllerTests;
import id.co.indivara.jdt12.najmi.nbank.controller.AtmControllerTests;
import id.co.indivara.jdt12.najmi.nbank.controller.AuthControllerTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import service.AccountServiceTests;

@SpringBootTest
@RunWith(Suite.class)

@Suite.SuiteClasses({
		AdminControllerTests.class,
		AtmControllerTests.class,
		AuthControllerTests.class,
		AtmControllerTests.class,
		AccountServiceTests.class
})
public class NbankApplicationTests {

}

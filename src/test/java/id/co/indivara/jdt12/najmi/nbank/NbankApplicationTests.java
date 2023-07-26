package id.co.indivara.jdt12.najmi.nbank;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@RunWith(Suite.class)

@Suite.SuiteClasses({
		AdminControllerTests.class,
		AtmControllerTests.class,
		AuthControllerTests.class,
		AtmControllerTests.class
})
public class NbankApplicationTests {

}

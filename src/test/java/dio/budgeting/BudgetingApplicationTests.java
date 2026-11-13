package dio.budgeting;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Sobe o contexto completo usando o banco H2 em memória,
 * então não precisa de Docker nem da chave da OpenAI.
 */
@SpringBootTest
@ActiveProfiles("h2")
class BudgetingApplicationTests {

    @Test
    void contextLoads() {
    }

}

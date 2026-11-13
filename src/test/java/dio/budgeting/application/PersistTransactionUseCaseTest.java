package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.support.InMemoryTransactionRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersistTransactionUseCaseTest {

    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
    private final PersistTransactionUseCase useCase = new PersistTransactionUseCase(repository);

    @Test
    void should_saveAndReturnValueInReais_when_inputIsValid() {
        var output = useCase.execute(new PersistTransactionInput("Farmácia", 3550, Category.PHARMA));

        assertEquals(35.5, output.value(), "3550 centavos devem virar R$ 35,50");
        assertEquals("PHARMA", output.category());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void should_notSave_when_inputIsInvalid() {
        assertThrows(InvalidTransactionException.class,
                () -> useCase.execute(new PersistTransactionInput("Mercado", 0, Category.GROCERIES)));

        assertTrue(repository.findAll().isEmpty());
    }
}

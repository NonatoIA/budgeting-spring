package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.support.InMemoryTransactionRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SummarizeTransactionsUseCaseTest {

    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
    private final SummarizeTransactionsUseCase useCase = new SummarizeTransactionsUseCase(repository);

    @Test
    void should_returnTotalsByCategory_orderedByHighestSpending() {
        repository.save(new Transaction("Mercado", 8000, Category.GROCERIES));
        repository.save(new Transaction("Feira", 4050, Category.GROCERIES));
        repository.save(new Transaction("Farmácia", 3500, Category.PHARMA));
        repository.save(new Transaction("Gasolina", 20000, Category.AUTO));

        var summary = useCase.execute();

        assertEquals(4, summary.transactionCount());
        assertEquals(355.5, summary.total());
        assertEquals(3, summary.categories().size());

        var first = summary.categories().get(0);
        assertEquals("AUTO", first.category());
        assertEquals(200.0, first.total());

        var groceries = summary.categories().get(1);
        assertEquals("GROCERIES", groceries.category());
        assertEquals(2, groceries.transactionCount());
        assertEquals(120.5, groceries.total());
    }

    @Test
    void should_returnZeroedSummary_when_thereAreNoTransactions() {
        var summary = useCase.execute();

        assertEquals(0, summary.transactionCount());
        assertEquals(0.0, summary.total());
        assertTrue(summary.categories().isEmpty());
    }
}

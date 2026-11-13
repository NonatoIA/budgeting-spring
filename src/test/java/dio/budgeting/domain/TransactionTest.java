package dio.budgeting.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void should_createTransaction_when_dataIsValid() {
        var transaction = new Transaction("  Farmácia  ", 3550, Category.PHARMA);

        assertNotNull(transaction.getId());
        assertEquals("Farmácia", transaction.getDescription());
        assertEquals(3550, transaction.getAmount());
        assertEquals(Category.PHARMA, transaction.getCategory());
    }

    @Test
    void should_reject_when_descriptionIsBlank() {
        var e = assertThrows(InvalidTransactionException.class,
                () -> new Transaction("   ", 1000, Category.GROCERIES));
        assertEquals("A descrição da transação é obrigatória.", e.getMessage());
    }

    @Test
    void should_reject_when_amountIsZeroOrNegative() {
        assertThrows(InvalidTransactionException.class, () -> new Transaction("Mercado", 0, Category.GROCERIES));
        assertThrows(InvalidTransactionException.class, () -> new Transaction("Mercado", -500, Category.GROCERIES));
    }

    @Test
    void should_reject_when_amountExceedsLimit() {
        assertThrows(InvalidTransactionException.class,
                () -> new Transaction("Carro novo", Transaction.MAX_AMOUNT_IN_CENTS + 1, Category.AUTO));
    }

    @Test
    void should_reject_when_categoryIsMissing() {
        assertThrows(InvalidTransactionException.class, () -> new Transaction("Mercado", 1000, null));
    }
}

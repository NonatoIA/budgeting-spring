package dio.budgeting.domain;

/**
 * Lançada quando uma transação viola alguma regra de negócio.
 */
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}

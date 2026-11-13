package dio.budgeting.application.output;

import dio.budgeting.domain.Transaction;

/**
 * Saída de uma transação. O campo value está em reais (ex.: 35.5 = R$ 35,50).
 */
public record TransactionOutput(String id, String description, String category, double value) {
    public static TransactionOutput from(Transaction transaction) {
        return new TransactionOutput(
                transaction.getId().uuid().toString(),
                transaction.getDescription(),
                transaction.getCategory().name(),
                Reais.fromCents(transaction.getAmount()));
    }
}

package dio.budgeting.application.output;

/**
 * Total gasto em uma categoria, em reais.
 */
public record CategorySummaryOutput(String category, long transactionCount, double total) {
}

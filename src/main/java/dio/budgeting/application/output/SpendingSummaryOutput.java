package dio.budgeting.application.output;

import java.util.List;

/**
 * Resumo geral dos gastos, em reais, com o detalhamento por categoria.
 */
public record SpendingSummaryOutput(long transactionCount, double total, List<CategorySummaryOutput> categories) {
}

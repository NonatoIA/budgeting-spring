package dio.budgeting.application;

import dio.budgeting.application.output.CategorySummaryOutput;
import dio.budgeting.application.output.Reais;
import dio.budgeting.application.output.SpendingSummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Nova consulta financeira: resumo dos gastos por categoria.
 * Usada tanto pelo endpoint REST quanto pela IA (Tool Calling).
 */
@Service
public class SummarizeTransactionsUseCase {
    private final TransactionRepository transactionRepository;

    public SummarizeTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "summarize-transactions",
            description = "Resume os gastos: total geral, quantidade de transações e total gasto em cada categoria, com valores em reais. "
                    + "Use quando perguntarem quanto foi gasto no total ou em qual categoria se gastou mais")
    public SpendingSummaryOutput execute() {
        List<Transaction> transactions = transactionRepository.findAll();

        Map<Category, List<Transaction>> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        () -> new EnumMap<>(Category.class),
                        Collectors.toList()));

        List<CategorySummaryOutput> categories = byCategory.entrySet().stream()
                .map(entry -> new CategorySummaryOutput(
                        entry.getKey().name(),
                        entry.getValue().size(),
                        Reais.fromCents(sumInCents(entry.getValue()))))
                .sorted(Comparator.comparingDouble(CategorySummaryOutput::total).reversed())
                .toList();

        return new SpendingSummaryOutput(
                transactions.size(),
                Reais.fromCents(sumInCents(transactions)),
                categories);
    }

    private static long sumInCents(List<Transaction> transactions) {
        return transactions.stream().mapToLong(Transaction::getAmount).sum();
    }
}

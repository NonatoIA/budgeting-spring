package dio.budgeting.application.input;

import dio.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.ToolParam;

public record PersistTransactionInput(@ToolParam(description = "Descrição curta do gasto, por exemplo: compras no mercado") String description,
                                      @ToolParam(description = "Valor do gasto em centavos. Exemplo: 35 reais e 50 centavos = 3550") long amount,
                                      @ToolParam(description = "Categoria da transação") Category category) {
}

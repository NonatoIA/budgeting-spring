package dio.budgeting.infrastructure.http.request;

/**
 * Comando em texto para o assistente, por exemplo: "gastei 35 reais na farmácia".
 */
public record AssistantRequest(String message) {
}

package dio.budgeting.infrastructure.http.response;

/**
 * Resposta do assistente, junto com o comando que foi interpretado.
 */
public record AssistantResponse(String message, String response) {
}

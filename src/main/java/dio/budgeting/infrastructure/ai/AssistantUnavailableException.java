package dio.budgeting.infrastructure.ai;

/**
 * Lançada quando o provedor de IA não responde (chave ausente ou inválida, sem créditos, falha de rede...).
 */
public class AssistantUnavailableException extends RuntimeException {

    public AssistantUnavailableException(Throwable cause) {
        super("O assistente de IA está indisponível no momento. "
                + "Verifique se a variável de ambiente OPENAI_API_KEY está configurada com uma chave válida e com créditos.", cause);
    }
}

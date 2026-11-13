package dio.budgeting.infrastructure.ai;

import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.SummarizeTransactionsUseCase;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Reúne as capacidades de IA (transcrição, chat com Tool Calling e geração de voz),
 * deixando o controller responsável apenas pelo HTTP.
 */
@Component
public class FinancialAssistant {

    private final ChatClient chatClient;
    private final TranscriptionModel transcriptionModel;
    private final TextToSpeechModel textToSpeechModel;

    public FinancialAssistant(ChatClient.Builder chatClientBuilder,
                              @Value("classpath:prompts/system-message.st") Resource systemPrompt,
                              TranscriptionModel transcriptionModel,
                              TextToSpeechModel textToSpeechModel,
                              PersistTransactionUseCase persistTransactionUseCase,
                              ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
                              SummarizeTransactionsUseCase summarizeTransactionsUseCase) throws IOException {
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt.getContentAsString(StandardCharsets.UTF_8))
                .defaultTools(persistTransactionUseCase, listTransactionsByCategoryUseCase, summarizeTransactionsUseCase)
                .build();
        this.transcriptionModel = transcriptionModel;
        this.textToSpeechModel = textToSpeechModel;
    }

    /** Envia o comando em texto para o modelo, que decide qual ferramenta (caso de uso) executar. */
    public String answer(String message) {
        try {
            return chatClient.prompt().user(message).call().content();
        } catch (RuntimeException e) {
            throw new AssistantUnavailableException(e);
        }
    }

    /** Converte o áudio enviado pela pessoa usuária em texto. */
    public String transcribe(Resource audio) {
        try {
            return transcriptionModel.transcribe(audio);
        } catch (RuntimeException e) {
            throw new AssistantUnavailableException(e);
        }
    }

    /** Converte a resposta final em áudio MP3. */
    public byte[] speak(String text) {
        try {
            return textToSpeechModel.call(text);
        } catch (RuntimeException e) {
            throw new AssistantUnavailableException(e);
        }
    }
}

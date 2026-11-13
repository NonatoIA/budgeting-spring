package dio.budgeting.infrastructure.http;

import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.SummarizeTransactionsUseCase;
import dio.budgeting.application.output.SpendingSummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.ai.FinancialAssistant;
import dio.budgeting.infrastructure.http.request.AssistantRequest;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import dio.budgeting.infrastructure.http.response.AssistantResponse;
import dio.budgeting.infrastructure.http.response.TransactionResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private static final MediaType AUDIO_MPEG = MediaType.parseMediaType("audio/mpeg");

    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;
    private final SummarizeTransactionsUseCase summarizeTransactionsUseCase;
    private final FinancialAssistant financialAssistant;

    public TransactionController(PersistTransactionUseCase persistTransactionUseCase,
                                 ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
                                 SummarizeTransactionsUseCase summarizeTransactionsUseCase,
                                 FinancialAssistant financialAssistant) {
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
        this.summarizeTransactionsUseCase = summarizeTransactionsUseCase;
        this.financialAssistant = financialAssistant;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@RequestBody TransactionRequest request) {
        var transaction = persistTransactionUseCase.execute(request.toInput());
        return TransactionResponse.from(transaction);
    }

    /** Novo: resumo dos gastos com total geral e total por categoria. */
    @GetMapping("/summary")
    public SpendingSummaryOutput summary() {
        return summarizeTransactionsUseCase.execute();
    }

    @GetMapping("/{category}")
    public List<TransactionResponse> readTransactions(@PathVariable Category category) {
        return listTransactionsByCategoryUseCase.execute(category).stream().map(TransactionResponse::from).toList();
    }

    /** Novo: o mesmo fluxo de IA, mas com texto. Facilita testar sem gravar áudio. */
    @PostMapping("/ai/text")
    public AssistantResponse chat(@RequestBody AssistantRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O campo message é obrigatório.");
        }
        var response = financialAssistant.answer(request.message());
        return new AssistantResponse(request.message(), response);
    }

    /** Fluxo principal: áudio -> texto -> IA com Tool Calling -> resposta em áudio (MP3). */
    @PostMapping(value = "/ai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> transcribe(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo de áudio no campo file.");
        }

        var userMessage = financialAssistant.transcribe(file.getResource());
        var result = financialAssistant.answer(userMessage);
        var audio = financialAssistant.speak(result);

        return ResponseEntity.ok()
                .contentType(AUDIO_MPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("audio.mp3")
                                .build()
                                .toString())
                .body(new ByteArrayResource(audio));
    }
}

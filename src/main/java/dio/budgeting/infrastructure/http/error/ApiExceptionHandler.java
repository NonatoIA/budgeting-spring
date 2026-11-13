package dio.budgeting.infrastructure.http.error;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.infrastructure.ai.AssistantUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Arrays;

/**
 * Converte erros em respostas HTTP claras, no formato padrão Problem Details (RFC 9457).
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String CATEGORIES = Arrays.toString(Category.values());

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ProblemDetail> invalidTransaction(InvalidTransactionException e) {
        return problem(HttpStatus.BAD_REQUEST, "Transação inválida", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> invalidParameter(MethodArgumentTypeMismatchException e) {
        return problem(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "Valor inválido para '" + e.getName() + "'. Categorias aceitas: " + CATEGORIES);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> unreadableBody(HttpMessageNotReadableException e) {
        return problem(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido",
                "Verifique o JSON enviado. Categorias aceitas: " + CATEGORIES);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> fileTooLarge(MaxUploadSizeExceededException e) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo muito grande", "O áudio deve ter no máximo 25 MB.");
    }

    @ExceptionHandler(AssistantUnavailableException.class)
    public ResponseEntity<ProblemDetail> assistantUnavailable(AssistantUnavailableException e) {
        log.error("Falha ao chamar o provedor de IA", e);
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Assistente indisponível", e.getMessage());
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail) {
        var body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(title);
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}

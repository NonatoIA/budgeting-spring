package dio.budgeting.domain;

/**
 * Transação financeira (um gasto). O valor é armazenado em centavos para evitar
 * problemas de arredondamento com números de ponto flutuante.
 */
public class Transaction {

    /** Limite de segurança: R$ 100.000,00. Evita registrar valores absurdos por erro de conversão. */
    public static final long MAX_AMOUNT_IN_CENTS = 10_000_000L;

    private final TransactionId id;
    private final String description;
    private final long amount;
    private final Category category;

    /**
     * Cria uma nova transação, aplicando as regras de negócio.
     */
    public Transaction(String description, long amount, Category category) {
        validate(description, amount, category);
        this.id = new TransactionId();
        this.description = description.trim();
        this.amount = amount;
        this.category = category;
    }

    /**
     * Reconstrói uma transação já existente (por exemplo, vinda do banco de dados).
     */
    public Transaction(TransactionId id, String description, long amount, Category category) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    private static void validate(String description, long amount, Category category) {
        if (description == null || description.isBlank()) {
            throw new InvalidTransactionException("A descrição da transação é obrigatória.");
        }
        if (amount <= 0) {
            throw new InvalidTransactionException("O valor da transação deve ser maior que zero.");
        }
        if (amount > MAX_AMOUNT_IN_CENTS) {
            throw new InvalidTransactionException("O valor da transação ultrapassa o limite de R$ 100.000,00.");
        }
        if (category == null) {
            throw new InvalidTransactionException("A categoria da transação é obrigatória.");
        }
    }

    public TransactionId getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }
}

package dio.budgeting.application.output;

import java.math.BigDecimal;

/**
 * Converte valores em centavos (como são armazenados) para reais (como são exibidos).
 */
public final class Reais {

    private Reais() {
    }

    public static double fromCents(long cents) {
        return BigDecimal.valueOf(cents, 2).doubleValue();
    }
}

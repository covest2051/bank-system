package kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private Long accountId;
    private Long cardId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
}
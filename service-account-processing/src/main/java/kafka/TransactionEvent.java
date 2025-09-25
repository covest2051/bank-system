package kafka;

import entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvent {
    private Long accountId;
    private Long cardId;
    private String type;
    private BigDecimal amount;
    private TransactionStatus status;
}

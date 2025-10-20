package kafka;

import entity.TransactionStatus;
import entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvent {
    private UUID eventUuid;
    private Long accountId;
    private Long cardId;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private OffsetDateTime occurredAt;
}

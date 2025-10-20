package kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardEvent {
    private Long clientId;
    private Long accountId;
    private String operation; // CREATE_CARD_REQUEST
}

package kafka;

import entity.PaymentRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditProductEvent {
    private PaymentRegistry paymentRegistry;
}

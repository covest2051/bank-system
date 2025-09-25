package kafka;

import clients.Ms1Client;
import dto.ClientInfoDto;
import entity.Decision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import service.CreditDecisionService;
import service.ProductRegistryService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientCreditProductEventListener {
    private final Ms1Client ms1Client;
    private final CreditDecisionService creditDecisionService;
    private final ProductRegistryService productRegistryService;

    @KafkaListener(topics = "client_credit_products")
    public void consume(Long clientId, Double interestRate, BigDecimal requestedAmount, Integer monthCount) {
        ClientInfoDto client = ms1Client.getClientInfo(clientId);
        log.info("Client {} {} with document {} requested CreditDecisionService", client.getFirstName(), client.getLastName(), client.getDocumentNumber());

        Decision decision = creditDecisionService.evaluate(clientId, requestedAmount);

        if(decision.isApproved()) {
            productRegistryService.openProduct(clientId, interestRate, requestedAmount, monthCount);
        } else {
            log.info("Failed to process request because of: {}", decision.getReason());
        }
    }
}

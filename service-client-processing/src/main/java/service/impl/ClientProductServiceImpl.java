package service.impl;

import dto.ClientProductDto;
import entity.ClientProduct;
import entity.Product;
import entity.ProductType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.ClientProductRepository;
import repository.ProductRepository;
import service.ClientProductService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientProductServiceImpl implements ClientProductService {
    private static final String TOPIC_CLIENT_PRODUCTS = "client_products";
    private static final String TOPIC_CLIENT_CREDIT_PRODUCTS = "client_credit_products";

    private final ClientProductRepository clientProductRepository;
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public ClientProduct create(ClientProduct cp) {
        ClientProduct saved = clientProductRepository.save(cp);

        sendEvent(saved, "CREATED");
        return saved;
    }

    @Override
    public ClientProduct update(Long id, ClientProduct cp) {
        ClientProduct clientProductForUpdate = clientProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ClientProduct not found: " + id));

        clientProductForUpdate.setCloseDate(cp.getCloseDate());
        clientProductForUpdate.setStatus(cp.getStatus());

        ClientProduct updated = clientProductRepository.save(clientProductForUpdate);

        sendEvent(updated, "UPDATED");
        return updated;
    }

    @Override
    public void delete(Long id) {
        Optional<ClientProduct> clientProductForDelete = clientProductRepository.findById(id);
        if (clientProductForDelete.isPresent()) {
            ClientProduct cp = clientProductForDelete.get();
            clientProductRepository.deleteById(id);
            sendEvent(cp, "DELETED");
        } else {
            throw new RuntimeException("ClientProduct not found: " + id);
        }
    }

    @Override
    public ClientProduct getById(Long id) {
        return clientProductRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
    }

    private void sendEvent(ClientProduct cp, String operation) {
        Long productId = Long.valueOf(cp.getProductId());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        ProductType type = product.getProductKey();

        String topic = chooseTopic(type);

        ClientProductDto event = ClientProductDto.builder()
                .clientId(cp.getClientId())
                .productId(cp.getProductId())
                .status(cp.getStatus())
                .openDate(cp.getOpenDate())
                .closeDate(cp.getCloseDate())
                .build();

        try {
            kafkaTemplate.send(topic, cp.getClientId(), event);
            log.info("Sent event to topic {} for clientId={}, productId={}, op={}", topic, cp.getClientId(), cp.getProductId(), operation);
        } catch (Exception e) {
            log.error("Failed to send event to topic {} for clientId={}, productId={}, op={}: {}", topic, cp.getClientId(), cp.getProductId(), operation, e.getMessage());
        }
    }

    private String chooseTopic(ProductType type) {
        switch (type) {
            case DC:
            case CC:
            case NS:
            case PENS:
                return TOPIC_CLIENT_PRODUCTS;
            case IPO:
            case PC:
            case AC:
                return TOPIC_CLIENT_CREDIT_PRODUCTS;
            default:
                return TOPIC_CLIENT_PRODUCTS;
        }
    }
}

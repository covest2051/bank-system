package clients;

import dto.ClientInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class Ms1Client {

    @Value("${client.service.url}")
    String clientServiceUrl;
    private final WebClient ms1WebClient;

    public ClientInfoDto getClientInfo(Long clientId) {
        return ms1WebClient.get()
                .uri(clientServiceUrl + "/api/clients/" + clientId)
                .retrieve()
                .bodyToMono(ClientInfoDto.class)
                .block(); // блокируем, чтобы получить объект синхронно
    }
}

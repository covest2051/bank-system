package aop.service;

import aop.dto.ErrorLog;
import aop.repository.ErrorLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorLogService {
    private final ErrorLogRepository errorLogRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public void saveFallback(String topic, String key, String payload, Map<String,String> headers, String serviceName) {
        ErrorLog e = new ErrorLog();
        e.setServiceName(serviceName);
        e.setTopic(topic);
        e.setKey(key);
        e.setPayload(payload);
        try {
            e.setHeaders(mapper.writeValueAsString(headers));
        } catch (JsonProcessingException ex) {
            e.setHeaders("{}");
        }
        errorLogRepository.save(e);
    }
}

package aop.aspect;

import aop.kafka.KafkaProducerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class HttpOutcomeRequestLogAspect {
    private final KafkaProducerService producerService;
    private final Environment environment;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String topic = "SERVICE_LOGS";

    public HttpOutcomeRequestLogAspect(KafkaProducerService producerService, Environment environment) {
        this.producerService = producerService;
        this.environment = environment;
    }

    @Pointcut("@annotation(aop.annotation.HttpOutcomeRequestLog)")
    public void annotatedMethods() {
    }

    @AfterReturning(pointcut = "annotatedMethods()", returning = "returnValue")
    public void afterHttpRequest(JoinPoint joinPoint, Object returnValue) {
        log.info("AFTER RETURNING {}",
                joinPoint.getSignature().toShortString());

        String microserviceName = environment.getProperty("spring.application.name");

        String payload = buildMessage(joinPoint, microserviceName, returnValue);

        Map<String, String> headers = new HashMap<>();

        headers.put("type", "INFO");

        producerService.sendMessageWithHeaders(topic, microserviceName, payload, headers);
    }

    private String buildMessage(JoinPoint joinPoint, String microserviceName, Object returnValue) {
        Map<String, Object> map = new HashMap<>();
        map.put("service", microserviceName);
        map.put("timestamp", Instant.now().toString());
        map.put("signature", joinPoint.getSignature().toShortString());
        map.put("args", joinPoint.getArgs());
        map.put("body", returnValue);

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof String && ((String) arg).startsWith("http")) {
                map.put("uri", arg);
                break;
            }
        }

        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize HTTP log", ex);
            return "{error:serialization failed}";
        }
    }
}

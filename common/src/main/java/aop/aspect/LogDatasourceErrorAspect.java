package aop.aspect;

import aop.annotation.LogDatasourceError;
import aop.kafka.KafkaProducerService;
import aop.service.ErrorLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class LogDatasourceErrorAspect {
    private final KafkaProducerService producerService;
    private final Environment environment;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorLogService errorLogService;

    final String topic = "SERVICE_LOGS";

    public LogDatasourceErrorAspect(KafkaProducerService producerService, Environment environment, ErrorLogService errorLogService) {
        this.producerService = producerService;
        this.environment = environment;
        this.errorLogService = errorLogService;
    }

    @Pointcut("@annotation(aop.annotation.LogDatasourceError)")
    public void annotatedMethods() {
    }

    @AfterThrowing(pointcut = "annotatedMethods()",
            throwing = "e")
    public void handleException(JoinPoint joinPoint, Exception e) {
        log.error("AFTER EXCEPTION {}",
                joinPoint.getSignature().toShortString());

        String microserviceName = environment.getProperty("spring.application.name");

        String payload = buildMessage(joinPoint, e, microserviceName);

        Map<String, String> headers = new HashMap<>();

        LogDatasourceError annotation = ((MethodSignature) joinPoint.getSignature())
                .getMethod()
                .getAnnotation(LogDatasourceError.class);
        String level = (annotation != null) ? annotation.value() : "ERROR";

        headers.put("type", level);

        try {
            producerService.sendMessageWithHeaders(topic, microserviceName, payload, headers);
        } catch (Exception ex) {
            log.error("Failed to send to Kafka topic {}, saving to DB fallback", topic, ex);
            errorLogService.saveFallback(topic, microserviceName, payload, headers, microserviceName);
        }

        log.error("Error occurred: ", e);
    }

    private String buildMessage(JoinPoint joinPoint, Exception e, String microserviceName) {
        Map<String, Object> map = new HashMap<>();
        map.put("service", microserviceName);
        map.put("timestamp", Instant.now().toString());
        map.put("signature", joinPoint.getSignature().toShortString());
        map.put("args", joinPoint.getArgs());
        map.put("exceptionClass", e.getClass().getName());
        map.put("message", e.getMessage());
        map.put("stacktrace", getStackTraceAsString(e));
        ;

        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException jsonEx) {
            log.error("Failed to serialize error message", jsonEx);
            return String.format("{service: %s, error: %s}", microserviceName, e.toString());
        }
    }

    private String getStackTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}

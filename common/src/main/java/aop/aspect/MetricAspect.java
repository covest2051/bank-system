package aop.aspect;

import aop.kafka.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Async
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MetricAspect {
    private final KafkaProducerService producerService;
    private final Environment environment;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${method.limit.duration}")
    private long methodLimitDuration;

    final String topic = "SERVICE_LOGS";
    private static final AtomicLong START_TIME = new AtomicLong();

    @Pointcut("@annotation(aop.annotation.Metric)")
    public void annotatedMethods() {
    }

//    @Before("@annotation(aop.annotation.Metric)")
//    public void logExecTime(JoinPoint joinPoint) throws Throwable {
//        log.info("Starting method: {}", joinPoint.getSignature().toShortString());
//        START_TIME.set(System.currentTimeMillis());
//    }
//
//    @After("@annotation(aop.annotation.Metric)")
//    public void calculateTime(JoinPoint joinPoint) {
//        long afterTime = System.currentTimeMillis();
//        log.info("Execution time: {} ms", (afterTime - START_TIME.get()));
//        START_TIME.set(0L);
//    }

    @Around("@annotation(aop.annotation.Metric)")
    public Object logExecTime(ProceedingJoinPoint pJoinPoint) throws Throwable {
        log.info("Calling method: {}", pJoinPoint.getSignature().toShortString());
        long beforeTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = pJoinPoint.proceed();//Important
        } finally {
            long afterTime = System.currentTimeMillis();
            long durationMs = afterTime - beforeTime;

            if (durationMs > methodLimitDuration) {
                sendExecutionTimeToKafka(pJoinPoint, durationMs);
            }

            log.info("Execution time: {} ms", (durationMs));
        }

        return result;
    }

    private void sendExecutionTimeToKafka(ProceedingJoinPoint joinPoint, long durationMs) {
        try {
            String method = joinPoint.getSignature().toShortString();

            String signature = joinPoint.getSignature().toShortString();
            String microserviceName = environment.getProperty("spring.application.name", "unknown-service");
            Object[] args = joinPoint.getArgs();

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("timestamp", Instant.now().toString());
            payloadMap.put("signature", signature);
            payloadMap.put("durationMs", durationMs);

            if (args != null && args.length > 0) {
                payloadMap.put("parameters", args);
            }

            String payloadJson;
            try {
                payloadJson = objectMapper.writeValueAsString(payloadMap);
            } catch (Exception ex) {
                log.warn("Failed to serialize metric payload for {}: {}", signature, ex.toString());
                payloadJson = String.format("{signature:%s,durationMs:%d}", signature, durationMs);
            }

            Map<String, String> headers = new HashMap<>();
            headers.put("type", "WARNING");

            producerService.sendMessageWithHeaders(topic, microserviceName, payloadJson, headers);

            log.debug("Metric sent to kafka topic={} key={} payload={}", topic, microserviceName, payloadJson);
        } catch (Exception e) {
            log.error("Error sending metric to Kafka", e);
        }
    }
}

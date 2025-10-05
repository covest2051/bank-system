package aop.aspect;

import aop.util.SimpleCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

@Async
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {
    private SimpleCache simpleCache = new SimpleCache();

    @Around("@annotation(aop.annotation.Cached)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();

        String methodSignature = pjp.getSignature().toLongString();

        int key = Objects.hash(methodSignature, Arrays.deepHashCode(args));

        Object cachedValue = simpleCache.get(key);
        if (cachedValue != null) {
            log.info("Returning cached result for key {}", key);
            return cachedValue;
        }

        Object result = pjp.proceed();

        simpleCache.put(key, result);
        return result;
    }
}

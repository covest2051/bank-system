package aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LogDatasourceErrorAspect {


    @Pointcut("@annotation(aop.annotation.LogDatasourceError)")
    public void annotatedMethods() {}
}

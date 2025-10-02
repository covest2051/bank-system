package aop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLogDto {
    private String serviceName;
    private OffsetDateTime timestamp;
    private String methodSignature;
    private String exceptionMessage;
    private String stackTrace;
    private String inputParams;
    private String severity;
}

package aop.handler;

import aop.dto.ErrorLogDto;

public interface ErrorLogHandler {
    void save(ErrorLogDto dto);
}

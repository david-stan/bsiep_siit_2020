package com.davidstan.domain;

import com.davidstan.domain.dto.LogDTO;

public interface LogObserver {
    void logHappened(LogDTO ldto);
}

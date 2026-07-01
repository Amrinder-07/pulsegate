package com.pulsegate.service;

import com.pulsegate.dto.DashboardSummaryResponse;
import com.pulsegate.dto.RequestLogResponse;
import com.pulsegate.model.RequestLog;
import com.pulsegate.repository.ApiKeyRepository;
import com.pulsegate.repository.RequestLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RequestLogService {

    private final RequestLogRepository requestLogRepository;
    private final ApiKeyRepository apiKeyRepository;

    public RequestLogService(RequestLogRepository requestLogRepository, ApiKeyRepository apiKeyRepository) {
        this.requestLogRepository = requestLogRepository;
        this.apiKeyRepository = apiKeyRepository;
    }

    @Transactional
    public void log(String apiKey, String method, String path, int statusCode, long latencyMs, String ipAddress, boolean blocked) {
        RequestLog requestLog = new RequestLog();
        requestLog.setApiKey(apiKey);
        requestLog.setMethod(method);
        requestLog.setPath(path);
        requestLog.setStatusCode(statusCode);
        requestLog.setLatencyMs(latencyMs);
        requestLog.setIpAddress(ipAddress);
        requestLog.setBlocked(blocked);
        requestLogRepository.save(requestLog);
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        return new DashboardSummaryResponse(
                requestLogRepository.count(),
                requestLogRepository.countSuccessful(),
                requestLogRepository.countFailed(),
                requestLogRepository.countByBlockedTrue(),
                Math.round(requestLogRepository.averageLatencyMs() * 10.0) / 10.0,
                apiKeyRepository.countByActiveTrue()
        );
    }

    @Transactional(readOnly = true)
    public List<RequestLogResponse> recentLogs(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return requestLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, safeLimit)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteAllLogs() {
        requestLogRepository.deleteAll();
    }

    private RequestLogResponse toResponse(RequestLog log) {
        return new RequestLogResponse(
                log.getId(),
                preview(log.getApiKey()),
                log.getMethod(),
                log.getPath(),
                log.getStatusCode(),
                log.getLatencyMs(),
                log.getIpAddress(),
                log.getTimestamp(),
                log.isBlocked()
        );
    }

    public static String preview(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "missing";
        }
        if (apiKey.length() <= 14) {
            return apiKey;
        }
        return apiKey.substring(0, 12) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}

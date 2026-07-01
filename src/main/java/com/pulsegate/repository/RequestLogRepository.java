package com.pulsegate.repository;

import com.pulsegate.model.RequestLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {

    List<RequestLog> findAllByOrderByTimestampDesc(Pageable pageable);

    long countByApiKey(String apiKey);

    @Query("select count(r) from RequestLog r where r.statusCode between 200 and 299")
    long countSuccessful();

    long countByBlockedTrue();

    @Query("select count(r) from RequestLog r where r.statusCode >= 400 and r.blocked = false")
    long countFailed();

    @Query("select coalesce(avg(r.latencyMs), 0) from RequestLog r")
    double averageLatencyMs();
}

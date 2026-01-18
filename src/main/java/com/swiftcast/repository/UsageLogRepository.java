package com.swiftcast.repository;

import com.swiftcast.model.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, String> {

    List<UsageLog> findByAccountIdOrderByTimestampDesc(String accountId);

    @Query("SELECT SUM(u.inputTokens) FROM UsageLog u WHERE u.accountId = :accountId")
    Long sumInputTokensByAccountId(String accountId);

    @Query("SELECT SUM(u.outputTokens) FROM UsageLog u WHERE u.accountId = :accountId")
    Long sumOutputTokensByAccountId(String accountId);

    @Query("SELECT SUM(u.costUsd) FROM UsageLog u WHERE u.accountId = :accountId")
    Double sumCostByAccountId(String accountId);
}

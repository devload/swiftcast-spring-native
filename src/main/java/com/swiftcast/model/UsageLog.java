package com.swiftcast.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "usage_logs")
@Data
@NoArgsConstructor
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(nullable = false)
    private String model;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "cost_usd")
    private Double costUsd;

    @Column(name = "request_path")
    private String requestPath;

    @Column(name = "status_code")
    private Integer statusCode;

    public UsageLog(String accountId, String model) {
        this.timestamp = Instant.now();
        this.accountId = accountId;
        this.model = model;
    }
}

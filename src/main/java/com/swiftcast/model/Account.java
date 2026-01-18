package com.swiftcast.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "base_url", nullable = false)
    private String baseUrl;

    @Column(name = "api_key", nullable = false)
    private String apiKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    public Account(String name, String baseUrl, String apiKey) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.createdAt = Instant.now();
        this.isActive = false;
    }
}

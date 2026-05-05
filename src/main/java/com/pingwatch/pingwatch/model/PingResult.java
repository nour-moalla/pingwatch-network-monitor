package com.pingwatch.pingwatch.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ping_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which host this result belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private MonitoredHost host;

    @Column(nullable = false)
    private boolean reachable;      // was the host reachable?

    private Double latencyMs;       // response time in milliseconds

    private int statusCode;         // HTTP status code (200, 404, 500...)

    private String errorMessage;    // error if unreachable

    @CreationTimestamp
    private LocalDateTime checkedAt;
}

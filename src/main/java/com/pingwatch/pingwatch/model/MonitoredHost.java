package com.pingwatch.pingwatch.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "monitored_hosts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredHost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hostname is required")
    @Column(nullable = false, unique = true)
    private String hostname;        // e.g. "server-01" or "192.168.1.10"

    @NotBlank(message = "IP address is required")
    @Column(nullable = false)
    private String ipAddress;       // e.g. "192.168.1.10"

    @Min(1) @Max(65535)
    private int port = 80;          // port to check (default 80)

    @Column(nullable = false)
    private String status = "UNKNOWN"; // ONLINE, OFFLINE, UNKNOWN

    private Double lastLatencyMs;   // last measured latency in ms

    private LocalDateTime lastChecked; // when was it last pinged

    @Column(nullable = false)
    private boolean active = true;  // is monitoring enabled?

    @CreationTimestamp
    private LocalDateTime registeredAt;

    private String description;     // optional description
}

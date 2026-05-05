package com.pingwatch.pingwatch.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pingwatch.pingwatch.model.MonitoredHost;

@Repository
public interface MonitoredHostRepository
        extends JpaRepository<MonitoredHost, Long> {

    // Spring Data JPA auto-generates SQL from method names
    List<MonitoredHost> findByStatus(String status);
    List<MonitoredHost> findByActive(boolean active);
    Optional<MonitoredHost> findByHostname(String hostname);
    boolean existsByHostname(String hostname);
}

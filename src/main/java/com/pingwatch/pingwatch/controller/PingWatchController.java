package com.pingwatch.pingwatch.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pingwatch.pingwatch.model.MonitoredHost;
import com.pingwatch.pingwatch.model.PingResult;
import com.pingwatch.pingwatch.service.PingWatchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PingWatchController {

    private final PingWatchService pingWatchService;

    // ============================================
    // HOST MANAGEMENT ENDPOINTS
    // ============================================

    // POST /api/hosts → register a new host to monitor
    @PostMapping("/hosts")
    public ResponseEntity<MonitoredHost> registerHost(
            @Valid @RequestBody MonitoredHost host) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(pingWatchService.registerHost(host));
    }

    // GET /api/hosts → list all monitored hosts
    @GetMapping("/hosts")
    public ResponseEntity<List<MonitoredHost>> getAllHosts() {
        return ResponseEntity.ok(pingWatchService.getAllHosts());
    }

    // GET /api/hosts/1 → get specific host
    @GetMapping("/hosts/{id}")
    public ResponseEntity<MonitoredHost> getHost(
            @PathVariable Long id) {
        return ResponseEntity.ok(pingWatchService.getHostById(id));
    }

    // PUT /api/hosts/1 → update host configuration
    @PutMapping("/hosts/{id}")
    public ResponseEntity<MonitoredHost> updateHost(
            @PathVariable Long id,
            @Valid @RequestBody MonitoredHost host) {
        return ResponseEntity.ok(
            pingWatchService.updateHost(id, host));
    }

    // DELETE /api/hosts/1 → remove host from monitoring
    @DeleteMapping("/hosts/{id}")
    public ResponseEntity<Void> deleteHost(@PathVariable Long id) {
        pingWatchService.deleteHost(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/hosts/status/ONLINE → hosts by status
    @GetMapping("/hosts/status/{status}")
    public ResponseEntity<List<MonitoredHost>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(
            pingWatchService.getHostsByStatus(status));
    }

    // ============================================
    // PING ENDPOINTS
    // ============================================

    // POST /api/hosts/1/ping → manually trigger a ping
    @PostMapping("/hosts/{id}/ping")
    public ResponseEntity<PingResult> pingHost(
            @PathVariable Long id) {
        return ResponseEntity.ok(pingWatchService.pingHost(id));
    }

    // GET /api/hosts/1/history → ping history for a host
    @GetMapping("/hosts/{id}/history")
    public ResponseEntity<List<PingResult>> getPingHistory(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            pingWatchService.getPingHistory(id));
    }

    // ============================================
    // STATISTICS ENDPOINTS
    // ============================================

    // GET /api/hosts/1/stats → uptime, latency stats for a host
    @GetMapping("/hosts/{id}/stats")
    public ResponseEntity<Map<String, Object>> getHostStats(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            pingWatchService.getHostStats(id));
    }

    // GET /api/dashboard → overall monitoring summary
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(
            pingWatchService.getDashboardSummary());
    }

    // GET /api/health → health check
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("PingWatch API is running 📡");
    }

    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> version() {
    Map<String, String> info = new HashMap<>();
    info.put("version", "2.0.0");
    info.put("name", "PingWatch");
    info.put("description", "Network Monitoring API");
    return ResponseEntity.ok(info);
    }
    
}
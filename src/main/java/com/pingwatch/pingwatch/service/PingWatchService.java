package com.pingwatch.pingwatch.service;

import com.pingwatch.pingwatch.model.MonitoredHost;
import com.pingwatch.pingwatch.model.PingResult;
import com.pingwatch.pingwatch.repository.MonitoredHostRepository;
import com.pingwatch.pingwatch.repository.PingResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PingWatchService {

    private final MonitoredHostRepository hostRepository;
    private final PingResultRepository pingResultRepository;
    private final PingEngine pingEngine;

    // ============================================
    // HOST MANAGEMENT
    // ============================================

    public MonitoredHost registerHost(MonitoredHost host) {
        if (hostRepository.existsByHostname(host.getHostname())) {
            throw new RuntimeException(
                "Host already registered: " + host.getHostname());
        }
        return hostRepository.save(host);
    }

    public List<MonitoredHost> getAllHosts() {
        return hostRepository.findAll();
    }

    public MonitoredHost getHostById(Long id) {
        return hostRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Host not found: " + id));
    }

    public MonitoredHost updateHost(Long id, MonitoredHost details) {
        MonitoredHost host = getHostById(id);
        host.setHostname(details.getHostname());
        host.setIpAddress(details.getIpAddress());
        host.setPort(details.getPort());
        host.setDescription(details.getDescription());
        host.setActive(details.isActive());
        return hostRepository.save(host);
    }

    public void deleteHost(Long id) {
        MonitoredHost host = getHostById(id);
        hostRepository.delete(host);
    }

    public List<MonitoredHost> getHostsByStatus(String status) {
        return hostRepository.findByStatus(status.toUpperCase());
    }

    // ============================================
    // PING OPERATIONS
    // ============================================

    // Manually trigger a ping check for a specific host
    public PingResult pingHost(Long id) {
        MonitoredHost host = getHostById(id);
        PingResult result = pingEngine.check(host);

        // Update host status and last check time
        host.setStatus(result.isReachable() ? "ONLINE" : "OFFLINE");
        host.setLastLatencyMs(result.getLatencyMs());
        host.setLastChecked(LocalDateTime.now());
        hostRepository.save(host);

        return pingResultRepository.save(result);
    }

    // Get ping history for a host
    public List<PingResult> getPingHistory(Long hostId) {
        getHostById(hostId); // validate host exists
        return pingResultRepository
            .findTop20ByHost_IdOrderByCheckedAtDesc(hostId);
    }

    // ============================================
    // STATISTICS
    // ============================================

    public Map<String, Object> getHostStats(Long hostId) {
        MonitoredHost host = getHostById(hostId);
        List<PingResult> history =
            pingResultRepository.findByHost_IdOrderByCheckedAtDesc(hostId);

        long totalChecks = history.size();
        long successfulChecks = pingResultRepository
            .countByHost_IdAndReachable(hostId, true);
        long failedChecks = totalChecks - successfulChecks;
        Double avgLatency = pingResultRepository
            .findAverageLatencyByHostId(hostId);

        double uptimePercent = totalChecks > 0
            ? (successfulChecks * 100.0) / totalChecks : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("hostname", host.getHostname());
        stats.put("currentStatus", host.getStatus());
        stats.put("totalChecks", totalChecks);
        stats.put("successfulChecks", successfulChecks);
        stats.put("failedChecks", failedChecks);
        stats.put("uptimePercent",
            Math.round(uptimePercent * 100.0) / 100.0);
        stats.put("averageLatencyMs",
            avgLatency != null
                ? Math.round(avgLatency * 100.0) / 100.0 : null);
        stats.put("lastLatencyMs", host.getLastLatencyMs());
        stats.put("lastChecked", host.getLastChecked());

        return stats;
    }

    public Map<String, Object> getDashboardSummary() {
        List<MonitoredHost> allHosts = hostRepository.findAll();
        long online = hostRepository.findByStatus("ONLINE").size();
        long offline = hostRepository.findByStatus("OFFLINE").size();
        long unknown = hostRepository.findByStatus("UNKNOWN").size();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalHosts", allHosts.size());
        summary.put("onlineHosts", online);
        summary.put("offlineHosts", offline);
        summary.put("unknownHosts", unknown);
        summary.put("uptimePercent",
            allHosts.isEmpty() ? 0
                : Math.round((online * 100.0 / allHosts.size())
                    * 100.0) / 100.0);

        return summary;
    }

    // ============================================
    // SCHEDULED AUTO-MONITORING
    // Automatically pings all active hosts every 60 seconds
    // ============================================
    @Scheduled(fixedDelay = 60000) // every 60 seconds
    public void autoMonitor() {
        List<MonitoredHost> activeHosts =
            hostRepository.findByActive(true);

        if (activeHosts.isEmpty()) return;

        log.info("🔄 Auto-monitoring {} active hosts...",
            activeHosts.size());

        for (MonitoredHost host : activeHosts) {
            try {
                pingHost(host.getId());
            } catch (Exception e) {
                log.error("Failed to check host {}: {}",
                    host.getHostname(), e.getMessage());
            }
        }

        log.info("✅ Auto-monitoring cycle complete");
    }
}

package com.pingwatch.pingwatch.service;

import com.pingwatch.pingwatch.model.MonitoredHost;
import com.pingwatch.pingwatch.model.PingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

@Component
@Slf4j
public class PingEngine {

    private static final int TIMEOUT_MS = 5000; // 5 seconds timeout

    /**
     * Check if a host is reachable and measure latency.
     * Tries HTTP first, falls back to ICMP ping.
     */
    public PingResult check(MonitoredHost host) {
        PingResult result = new PingResult();
        result.setHost(host);

        long startTime = System.currentTimeMillis();

        try {
            // Try HTTP check first (most accurate for web services)
            String url = "http://" + host.getIpAddress()
                + ":" + host.getPort();

            HttpURLConnection connection =
                (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestMethod("HEAD"); // lightweight request

            int statusCode = connection.getResponseCode();
            long latency = System.currentTimeMillis() - startTime;

            result.setReachable(statusCode < 500);
            result.setLatencyMs((double) latency);
            result.setStatusCode(statusCode);

            log.info("✅ {} - {}ms (HTTP {})",
                host.getHostname(), latency, statusCode);

        } catch (Exception httpException) {
            // HTTP failed — try ICMP ping
            try {
                InetAddress address =
                    InetAddress.getByName(host.getIpAddress());
                boolean reachable = address.isReachable(TIMEOUT_MS);
                long latency = System.currentTimeMillis() - startTime;

                result.setReachable(reachable);
                result.setLatencyMs(reachable ? (double) latency : null);
                result.setStatusCode(reachable ? 0 : -1);

                if (reachable) {
                    log.info("✅ {} reachable via ICMP - {}ms",
                        host.getHostname(), latency);
                } else {
                    log.warn("❌ {} unreachable", host.getHostname());
                    result.setErrorMessage("Host unreachable");
                }

            } catch (Exception icmpException) {
                long latency = System.currentTimeMillis() - startTime;
                result.setReachable(false);
                result.setLatencyMs((double) latency);
                result.setStatusCode(-1);
                result.setErrorMessage(icmpException.getMessage());
                log.error("❌ {} check failed: {}",
                    host.getHostname(), icmpException.getMessage());
            }
        }

        return result;
    }
}
package com.pingwatch.pingwatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pingwatch.pingwatch.model.PingResult;

@Repository
public interface PingResultRepository
        extends JpaRepository<PingResult, Long> {

    List<PingResult> findTop20ByHost_IdOrderByCheckedAtDesc(Long hostId);

    List<PingResult> findByHost_IdOrderByCheckedAtDesc(Long hostId);

    long countByHost_IdAndReachable(Long hostId, boolean reachable);

    @Query("SELECT AVG(p.latencyMs) FROM PingResult p " +
           "WHERE p.host.id = :hostId AND p.reachable = true")
    Double findAverageLatencyByHostId(@Param("hostId") Long hostId);
}
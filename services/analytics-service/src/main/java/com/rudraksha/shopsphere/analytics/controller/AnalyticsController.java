package com.rudraksha.shopsphere.analytics.controller;

import com.rudraksha.shopsphere.analytics.dto.AnalyticsResponse;
import com.rudraksha.shopsphere.analytics.service.AnalyticsService;
import com.rudraksha.shopsphere.shared.models.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/events")
    public ResponseEntity<ApiResponse<String>> ingestEvent(
            @RequestParam String eventType,
            @RequestParam Long userId,
            @RequestParam String sessionId,
            @RequestBody Map<String, Object> eventData,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {

        log.info("Ingest analytics event - Type: {}", eventType);
        analyticsService.ingestEvent(eventType, userId, sessionId, eventData, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("Event ingested successfully"));
    }

    @GetMapping("/events/{eventType}")
    public ResponseEntity<ApiResponse<List<AnalyticsResponse>>> getEventsByType(
            @PathVariable String eventType) {
        log.info("Get events by type - Type: {}", eventType);
        List<AnalyticsResponse> events = analyticsService.getEventsByType(eventType);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/users/{userId}/events")
    public ResponseEntity<ApiResponse<List<AnalyticsResponse>>> getUserEvents(
            @PathVariable Long userId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        log.info("Get user events - User: {}", userId);
        List<AnalyticsResponse> events = analyticsService.getUserEvents(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/events/recent")
    public ResponseEntity<ApiResponse<List<AnalyticsResponse>>> getRecentEvents(
            @RequestParam String eventType,
            @RequestParam LocalDateTime since) {
        log.info("Get recent events - Type: {}", eventType);
        List<AnalyticsResponse> events = analyticsService.getRecentEvents(eventType, since);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTotalEventCount() {
        log.info("Get total event count");
        long count = analyticsService.getTotalEventCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/count/{eventType}")
    public ResponseEntity<ApiResponse<Long>> getEventCountByType(
            @PathVariable String eventType) {
        log.info("Get event count by type - Type: {}", eventType);
        long count = analyticsService.getEventCountByType(eventType);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getEventById(
            @PathVariable String eventId) {
        log.info("Get event by ID - ID: {}", eventId);
        AnalyticsResponse event = analyticsService.getEventById(eventId);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @PostMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Analytics Service is running"));
    }
}

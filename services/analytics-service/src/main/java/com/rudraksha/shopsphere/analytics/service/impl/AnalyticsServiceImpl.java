package com.rudraksha.shopsphere.analytics.service.impl;

import com.rudraksha.shopsphere.analytics.document.AnalyticsEvent;
import com.rudraksha.shopsphere.analytics.dto.AnalyticsResponse;
import com.rudraksha.shopsphere.analytics.repository.AnalyticsEventRepository;
import com.rudraksha.shopsphere.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsEventRepository analyticsEventRepository;

    @Override
    public void ingestEvent(String eventType, Long userId, String sessionId, Map<String, Object> eventData, String ipAddress, String userAgent) {
        log.info("Ingesting analytics event - Type: {}, User: {}, Session: {}", eventType, userId, sessionId);

        AnalyticsEvent event = AnalyticsEvent.builder()
            .eventType(eventType)
            .userId(userId)
            .sessionId(sessionId)
            .eventData(eventData)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .timestamp(LocalDateTime.now())
            .processed(false)
            .build();

        analyticsEventRepository.save(event);
        log.info("Analytics event saved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalyticsResponse> getEventsByType(String eventType) {
        log.info("Fetching events of type: {}", eventType);
        return analyticsEventRepository.findByEventType(eventType)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalyticsResponse> getUserEvents(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching user events - User: {}, From: {} to {}", userId, startDate, endDate);
        return analyticsEventRepository.findUserEventsInDateRange(userId, startDate, endDate)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalyticsResponse> getRecentEvents(String eventType, LocalDateTime since) {
        log.info("Fetching recent events - Type: {}, Since: {}", eventType, since);
        return analyticsEventRepository.findRecentEventsByType(eventType, since)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalEventCount() {
        log.info("Fetching total event count");
        return analyticsEventRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getEventCountByType(String eventType) {
        log.info("Fetching event count for type: {}", eventType);
        return analyticsEventRepository.findByEventType(eventType).size();
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getEventById(String eventId) {
        log.info("Fetching event by ID: {}", eventId);
        return analyticsEventRepository.findById(eventId)
            .map(this::mapToResponse)
            .orElseThrow(() -> new RuntimeException("Analytics event not found with ID: " + eventId));
    }

    private AnalyticsResponse mapToResponse(AnalyticsEvent event) {
        return AnalyticsResponse.builder()
            .id(event.getId())
            .eventType(event.getEventType())
            .userId(event.getUserId())
            .sessionId(event.getSessionId())
            .eventData(event.getEventData())
            .timestamp(event.getTimestamp())
            .processed(event.isProcessed())
            .build();
    }
}

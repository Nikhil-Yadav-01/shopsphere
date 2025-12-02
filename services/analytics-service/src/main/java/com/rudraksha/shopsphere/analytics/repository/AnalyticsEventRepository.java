package com.rudraksha.shopsphere.analytics.repository;

import com.rudraksha.shopsphere.analytics.document.AnalyticsEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsEventRepository extends MongoRepository<AnalyticsEvent, String> {

    @Query("{ 'eventType': ?0 }")
    List<AnalyticsEvent> findByEventType(String eventType);

    @Query("{ 'userId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<AnalyticsEvent> findUserEventsInDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'timestamp': { $gte: ?0, $lte: ?1 }, 'processed': false }")
    List<AnalyticsEvent> findUnprocessedEventsInDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'eventType': ?0, 'timestamp': { $gte: ?1 } }")
    List<AnalyticsEvent> findRecentEventsByType(String eventType, LocalDateTime since);
}

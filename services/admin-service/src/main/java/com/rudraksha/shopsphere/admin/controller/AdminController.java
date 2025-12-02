package com.rudraksha.shopsphere.admin.controller;

import com.rudraksha.shopsphere.admin.dto.response.AuditLogResponse;
import com.rudraksha.shopsphere.admin.dto.response.SystemMetricsResponse;
import com.rudraksha.shopsphere.admin.service.AdminService;
import com.rudraksha.shopsphere.shared.models.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam Long adminId,
            Pageable pageable) {
        log.info("Get audit logs request - adminId: {}", adminId);
        Page<AuditLogResponse> logs = adminService.getAuditLogs(adminId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/audit-logs/action/{action}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogsByAction(
            @PathVariable String action,
            Pageable pageable) {
        log.info("Get audit logs by action request - action: {}", action);
        Page<AuditLogResponse> logs = adminService.getAuditLogsByAction(action, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/audit-logs/date-range")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        log.info("Get audit logs by date range - {} to {}", startDate, endDate);
        List<AuditLogResponse> logs = adminService.getAuditLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/audit-logs/resource/{resourceType}/{resourceId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByResource(
            @PathVariable String resourceType,
            @PathVariable Long resourceId) {
        log.info("Get audit logs by resource - type: {}, id: {}", resourceType, resourceId);
        List<AuditLogResponse> logs = adminService.getAuditLogsByResource(resourceType, resourceId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<List<SystemMetricsResponse>>> getMetrics(
            @RequestParam String metricType,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        log.info("Get metrics request - type: {}", metricType);
        List<SystemMetricsResponse> metrics = adminService.getSystemMetrics(metricType, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/metrics/recent")
    public ResponseEntity<ApiResponse<List<SystemMetricsResponse>>> getRecentMetrics(
            @RequestParam LocalDateTime since) {
        log.info("Get recent metrics request - since: {}", since);
        List<SystemMetricsResponse> metrics = adminService.getRecentMetrics(since);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @PostMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Admin Service is running"));
    }
}

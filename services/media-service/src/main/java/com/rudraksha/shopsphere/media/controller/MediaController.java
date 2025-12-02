package com.rudraksha.shopsphere.media.controller;

import com.rudraksha.shopsphere.media.dto.request.UpdateMediaRequest;
import com.rudraksha.shopsphere.media.dto.request.UploadMediaRequest;
import com.rudraksha.shopsphere.media.dto.response.MediaResponse;
import com.rudraksha.shopsphere.media.service.MediaService;
import com.rudraksha.shopsphere.shared.models.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<MediaResponse>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary) {

        log.info("Upload media request - entityType: {}, entityId: {}", entityType, entityId);

        UploadMediaRequest request = UploadMediaRequest.builder()
                .file(file)
                .entityType(entityType)
                .entityId(entityId)
                .altText(altText)
                .isPrimary(isPrimary)
                .build();

        MediaResponse response = mediaService.uploadMedia(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Media uploaded successfully", response));
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<ApiResponse<MediaResponse>> getMedia(@PathVariable Long mediaId) {
        log.info("Get media request - mediaId: {}", mediaId);
        MediaResponse response = mediaService.getMedia(mediaId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<MediaResponse>>> getMediaByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {

        log.info("Get media by entity request - entityType: {}, entityId: {}", entityType, entityId);
        List<MediaResponse> response = mediaService.getMediaByEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/entity/{entityType}/{entityId}/primary")
    public ResponseEntity<ApiResponse<MediaResponse>> getPrimaryMedia(
            @PathVariable String entityType,
            @PathVariable Long entityId) {

        log.info("Get primary media request - entityType: {}, entityId: {}", entityType, entityId);
        MediaResponse response = mediaService.getPrimaryMedia(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{mediaId}")
    public ResponseEntity<ApiResponse<MediaResponse>> updateMedia(
            @PathVariable Long mediaId,
            @RequestBody @Valid UpdateMediaRequest request) {

        log.info("Update media request - mediaId: {}", mediaId);
        MediaResponse response = mediaService.updateMedia(mediaId, request);
        return ResponseEntity.ok(ApiResponse.success("Media updated successfully", response));
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<ApiResponse<String>> deleteMedia(@PathVariable Long mediaId) {
        log.info("Delete media request - mediaId: {}", mediaId);
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.ok(ApiResponse.success("Media deleted successfully"));
    }

    @DeleteMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<String>> deleteMediaByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {

        log.info("Delete media by entity request - entityType: {}, entityId: {}", entityType, entityId);
        mediaService.deleteMediaByEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success("Media deleted successfully"));
    }

    @GetMapping("/upload-url")
    public ResponseEntity<ApiResponse<String>> getUploadUrl(
            @RequestParam String fileName,
            @RequestParam(required = false, defaultValue = "image/jpeg") String contentType) {

        log.info("Get upload URL request - fileName: {}", fileName);
        String uploadUrl = mediaService.getUploadUrl(fileName, contentType);
        return ResponseEntity.ok(ApiResponse.success(uploadUrl));
    }

    @PostMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Media Service is running"));
    }
}

package com.rudraksha.shopsphere.media.service.impl;

import com.rudraksha.shopsphere.media.dto.request.UpdateMediaRequest;
import com.rudraksha.shopsphere.media.dto.request.UploadMediaRequest;
import com.rudraksha.shopsphere.media.dto.response.MediaResponse;
import com.rudraksha.shopsphere.media.entity.Media;
import com.rudraksha.shopsphere.media.exception.InvalidMediaException;
import com.rudraksha.shopsphere.media.exception.MediaNotFoundException;
import com.rudraksha.shopsphere.media.repository.MediaRepository;
import com.rudraksha.shopsphere.media.service.MediaService;
import com.rudraksha.shopsphere.media.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final S3Service s3Service;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    private static final String[] ALLOWED_VIDEO_TYPES = {"video/mp4", "video/quicktime", "video/x-msvideo"};

    @Override
    public MediaResponse uploadMedia(UploadMediaRequest request) {
        log.info("Uploading media for entity: {} with ID: {}", request.getEntityType(), request.getEntityId());

        MultipartFile file = request.getFile();
        validateMediaFile(file);

        try {
            String s3Key = generateS3Key(request.getEntityType(), request.getEntityId(), file.getOriginalFilename());
            String url = s3Service.uploadFile(s3Key, file);

            Media media = Media.builder()
                    .url(url)
                    .fileName(Objects.requireNonNull(file.getOriginalFilename()))
                    .fileType(extractFileExtension(file.getOriginalFilename()))
                    .fileSize(file.getSize())
                    .mediaType(determineMediaType(file.getContentType()))
                    .entityType(request.getEntityType())
                    .entityId(request.getEntityId())
                    .altText(request.getAltText())
                    .isPrimary(request.getIsPrimary())
                    .build();

            // If marking as primary, unmark other primaries
            if (Boolean.TRUE.equals(request.getIsPrimary())) {
                mediaRepository.findByEntityTypeAndEntityId(request.getEntityType(), request.getEntityId())
                        .forEach(m -> {
                            m.setIsPrimary(false);
                            mediaRepository.save(m);
                        });
            }

            Media savedMedia = mediaRepository.save(media);
            log.info("Media uploaded successfully with ID: {}", savedMedia.getId());
            return MediaResponse.from(savedMedia);
        } catch (Exception e) {
            log.error("Error uploading media: {}", e.getMessage(), e);
            throw new InvalidMediaException("Failed to upload media: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MediaResponse getMedia(Long mediaId) {
        log.info("Fetching media with ID: {}", mediaId);
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new MediaNotFoundException(mediaId));
        return MediaResponse.from(media);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaResponse> getMediaByEntity(String entityType, Long entityId) {
        log.info("Fetching media for entity: {} with ID: {}", entityType, entityId);
        return mediaRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(MediaResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MediaResponse getPrimaryMedia(String entityType, Long entityId) {
        log.info("Fetching primary media for entity: {} with ID: {}", entityType, entityId);
        Media media = mediaRepository.findPrimaryByEntityTypeAndEntityId(entityType, entityId)
                .orElseThrow(() -> new MediaNotFoundException("Primary media not found for entity"));
        return MediaResponse.from(media);
    }

    @Override
    public MediaResponse updateMedia(Long mediaId, UpdateMediaRequest request) {
        log.info("Updating media with ID: {}", mediaId);
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new MediaNotFoundException(mediaId));

        if (request.getAltText() != null) {
            media.setAltText(request.getAltText());
        }

        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            // Unmark other primaries
            mediaRepository.findByEntityTypeAndEntityId(media.getEntityType(), media.getEntityId())
                    .forEach(m -> {
                        if (!m.getId().equals(mediaId)) {
                            m.setIsPrimary(false);
                            mediaRepository.save(m);
                        }
                    });
            media.setIsPrimary(true);
        }

        Media updatedMedia = mediaRepository.save(media);
        log.info("Media updated successfully with ID: {}", mediaId);
        return MediaResponse.from(updatedMedia);
    }

    @Override
    public void deleteMedia(Long mediaId) {
        log.info("Deleting media with ID: {}", mediaId);
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new MediaNotFoundException(mediaId));

        try {
            // Delete from S3
            s3Service.deleteFile(extractS3KeyFromUrl(media.getUrl()));
            // Delete from database
            mediaRepository.deleteById(mediaId);
            log.info("Media deleted successfully with ID: {}", mediaId);
        } catch (Exception e) {
            log.error("Error deleting media: {}", e.getMessage(), e);
            throw new InvalidMediaException("Failed to delete media: " + e.getMessage());
        }
    }

    @Override
    public void deleteMediaByEntity(String entityType, Long entityId) {
        log.info("Deleting all media for entity: {} with ID: {}", entityType, entityId);
        List<Media> mediaList = mediaRepository.findByEntityTypeAndEntityId(entityType, entityId);

        for (Media media : mediaList) {
            try {
                s3Service.deleteFile(extractS3KeyFromUrl(media.getUrl()));
            } catch (Exception e) {
                log.warn("Failed to delete S3 file for media ID {}: {}", media.getId(), e.getMessage());
            }
        }

        mediaRepository.deleteByEntityTypeAndEntityId(entityType, entityId);
        log.info("All media deleted for entity: {} with ID: {}", entityType, entityId);
    }

    @Override
    public String getUploadUrl(String fileName, String contentType) {
        return s3Service.generatePresignedUploadUrl(fileName, contentType);
    }

    @Override
    public void validateMediaFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidMediaException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidMediaException("File size exceeds maximum allowed size of 50MB");
        }

        String contentType = file.getContentType();
        if (!isValidContentType(contentType)) {
            throw new InvalidMediaException("File type not supported: " + contentType);
        }
    }

    private boolean isValidContentType(String contentType) {
        if (contentType == null) return false;

        for (String type : ALLOWED_IMAGE_TYPES) {
            if (contentType.equals(type)) return true;
        }
        for (String type : ALLOWED_VIDEO_TYPES) {
            if (contentType.equals(type)) return true;
        }
        return false;
    }

    private Media.MediaType determineMediaType(String contentType) {
        if (contentType == null) return Media.MediaType.IMAGE;

        if (contentType.startsWith("image/")) return Media.MediaType.IMAGE;
        if (contentType.startsWith("video/")) return Media.MediaType.VIDEO;
        return Media.MediaType.DOCUMENT;
    }

    private String generateS3Key(String entityType, Long entityId, String fileName) {
        return String.format("media/%s/%d/%s", entityType.toLowerCase(), entityId, System.nanoTime() + "-" + fileName);
    }

    private String extractFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private String extractS3KeyFromUrl(String url) {
        if (url == null || !url.contains("media/")) {
            return url;
        }
        return url.substring(url.indexOf("media/"));
    }
}

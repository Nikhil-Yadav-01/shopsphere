package com.rudraksha.shopsphere.media.dto.response;

import com.rudraksha.shopsphere.media.entity.Media;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaResponse {

    private Long id;
    private String url;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String mediaType;
    private String entityType;
    private Long entityId;
    private String altText;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MediaResponse from(Media media) {
        return MediaResponse.builder()
                .id(media.getId())
                .url(media.getUrl())
                .fileName(media.getFileName())
                .fileType(media.getFileType())
                .fileSize(media.getFileSize())
                .mediaType(media.getMediaType().toString())
                .entityType(media.getEntityType())
                .entityId(media.getEntityId())
                .altText(media.getAltText())
                .isPrimary(media.getIsPrimary())
                .createdAt(media.getCreatedAt())
                .updatedAt(media.getUpdatedAt())
                .build();
    }
}

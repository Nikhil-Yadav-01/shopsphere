package com.rudraksha.shopsphere.media.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadMediaRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotBlank(message = "Entity type is required")
    private String entityType; // PRODUCT, REVIEW, CATEGORY

    @NotNull(message = "Entity ID is required")
    private Long entityId;

    private String altText;

    private Boolean isPrimary = false;
}

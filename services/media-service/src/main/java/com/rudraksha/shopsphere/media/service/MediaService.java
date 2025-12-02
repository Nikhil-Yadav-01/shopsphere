package com.rudraksha.shopsphere.media.service;

import com.rudraksha.shopsphere.media.dto.request.UpdateMediaRequest;
import com.rudraksha.shopsphere.media.dto.request.UploadMediaRequest;
import com.rudraksha.shopsphere.media.dto.response.MediaResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {

    MediaResponse uploadMedia(UploadMediaRequest request);

    MediaResponse getMedia(Long mediaId);

    List<MediaResponse> getMediaByEntity(String entityType, Long entityId);

    MediaResponse getPrimaryMedia(String entityType, Long entityId);

    MediaResponse updateMedia(Long mediaId, UpdateMediaRequest request);

    void deleteMedia(Long mediaId);

    void deleteMediaByEntity(String entityType, Long entityId);

    String getUploadUrl(String fileName, String contentType);

    void validateMediaFile(MultipartFile file);
}

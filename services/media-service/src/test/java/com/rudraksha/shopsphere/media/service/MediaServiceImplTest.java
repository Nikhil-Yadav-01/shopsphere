package com.rudraksha.shopsphere.media.service;

import com.rudraksha.shopsphere.media.dto.request.UpdateMediaRequest;
import com.rudraksha.shopsphere.media.dto.request.UploadMediaRequest;
import com.rudraksha.shopsphere.media.dto.response.MediaResponse;
import com.rudraksha.shopsphere.media.entity.Media;
import com.rudraksha.shopsphere.media.exception.MediaNotFoundException;
import com.rudraksha.shopsphere.media.repository.MediaRepository;
import com.rudraksha.shopsphere.media.service.impl.MediaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private S3Service s3Service;

    private MediaServiceImpl mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaServiceImpl(mediaRepository, s3Service);
    }

    @Test
    void testUploadMedia_Success() {
        // Arrange
        byte[] content = "test image content".getBytes();
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", content);

        UploadMediaRequest request = UploadMediaRequest.builder()
                .file(file)
                .entityType("PRODUCT")
                .entityId(1L)
                .altText("Test Image")
                .isPrimary(true)
                .build();

        when(s3Service.uploadFile(anyString(), eq(file)))
                .thenReturn("https://s3.amazonaws.com/media/test.jpg");

        Media savedMedia = Media.builder()
                .id(1L)
                .url("https://s3.amazonaws.com/media/test.jpg")
                .fileName("test.jpg")
                .fileType("jpg")
                .fileSize(file.getSize())
                .mediaType(Media.MediaType.IMAGE)
                .entityType("PRODUCT")
                .entityId(1L)
                .altText("Test Image")
                .isPrimary(true)
                .build();

        when(mediaRepository.findByEntityTypeAndEntityId("PRODUCT", 1L)).thenReturn(List.of());
        when(mediaRepository.save(any(Media.class))).thenReturn(savedMedia);

        // Act
        MediaResponse response = mediaService.uploadMedia(request);

        // Assert
        assertNotNull(response);
        assertEquals("test.jpg", response.getFileName());
        assertEquals("PRODUCT", response.getEntityType());
        verify(mediaRepository, times(1)).save(any(Media.class));
    }

    @Test
    void testGetMedia_Success() {
        // Arrange
        Media media = Media.builder()
                .id(1L)
                .url("https://s3.amazonaws.com/media/test.jpg")
                .fileName("test.jpg")
                .build();

        when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));

        // Act
        MediaResponse response = mediaService.getMedia(1L);

        // Assert
        assertNotNull(response);
        assertEquals("test.jpg", response.getFileName());
        verify(mediaRepository, times(1)).findById(1L);
    }

    @Test
    void testGetMedia_NotFound() {
        // Arrange
        when(mediaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MediaNotFoundException.class, () -> mediaService.getMedia(1L));
    }

    @Test
    void testUpdateMedia_Success() {
        // Arrange
        Media existingMedia = Media.builder()
                .id(1L)
                .altText("Old Text")
                .isPrimary(false)
                .entityType("PRODUCT")
                .entityId(1L)
                .build();

        UpdateMediaRequest request = UpdateMediaRequest.builder()
                .altText("New Text")
                .isPrimary(true)
                .build();

        when(mediaRepository.findById(1L)).thenReturn(Optional.of(existingMedia));
        when(mediaRepository.findByEntityTypeAndEntityId("PRODUCT", 1L)).thenReturn(List.of(existingMedia));
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MediaResponse response = mediaService.updateMedia(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals("New Text", response.getAltText());
        assertTrue(response.getIsPrimary());
        verify(mediaRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteMedia_Success() {
        // Arrange
        Media media = Media.builder()
                .id(1L)
                .url("https://s3.amazonaws.com/media/test.jpg")
                .build();

        when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));

        // Act
        mediaService.deleteMedia(1L);

        // Assert
        verify(s3Service, times(1)).deleteFile(anyString());
        verify(mediaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetMediaByEntity_Success() {
        // Arrange
        Media media = Media.builder()
                .id(1L)
                .entityType("PRODUCT")
                .entityId(1L)
                .build();

        when(mediaRepository.findByEntityTypeAndEntityId("PRODUCT", 1L))
                .thenReturn(List.of(media));

        // Act
        List<MediaResponse> response = mediaService.getMediaByEntity("PRODUCT", 1L);

        // Assert
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        verify(mediaRepository, times(1)).findByEntityTypeAndEntityId("PRODUCT", 1L);
    }
}

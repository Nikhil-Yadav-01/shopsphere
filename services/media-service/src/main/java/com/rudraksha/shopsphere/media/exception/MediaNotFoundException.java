package com.rudraksha.shopsphere.media.exception;

public class MediaNotFoundException extends RuntimeException {
    public MediaNotFoundException(String message) {
        super(message);
    }

    public MediaNotFoundException(Long mediaId) {
        super("Media not found with ID: " + mediaId);
    }
}
